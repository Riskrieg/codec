/*
 *     Riskrieg, an open-source conflict simulation game.
 *     Copyright (C) 2021-2022 Aaron Yoder <aaronjyoder@gmail.com> and the Riskrieg contributors
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.riskrieg.codec.encode;

import com.riskrieg.codec.RkmField;
import com.riskrieg.map.RkmMap;
import com.riskrieg.map.Territory;
import com.riskrieg.map.territory.Border;
import com.riskrieg.map.territory.Nucleus;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import javax.imageio.ImageIO;

public final class RkmEncoder implements Encoder<RkmMap> {

  // 8B: File signature. HEX: 83 52 4B 4D 0D 0A 1A 0A -- \131 R K M \r \n \032 \n
  private final byte[] signature = new byte[]{(byte) 0x83, (byte) 0x52, (byte) 0x4B, (byte) 0x4D, (byte) 0x0D, (byte) 0x0A, (byte) 0x1A, (byte) 0x0A};

  @Override
  public void encode(RkmMap map, OutputStream outputStream, boolean shouldCloseStream) throws IOException, NoSuchAlgorithmException {
    Objects.requireNonNull(map);
    Objects.requireNonNull(outputStream);

    writeSignature(outputStream);
    writeField(RkmField.MAP_CODE_NAME, map.codename().getBytes(StandardCharsets.UTF_8), outputStream);
    writeField(RkmField.MAP_DISPLAY_NAME, map.displayName().getBytes(StandardCharsets.UTF_8), outputStream);
    writeField(RkmField.MAP_AUTHOR_NAME, map.author().getBytes(StandardCharsets.UTF_8), outputStream);

    writeVertices(map.vertices().toArray(new Territory[0]), outputStream);
    writeEdges(map.edges().toArray(new Border[0]), outputStream);

    writeBaseImageLayer(map.baseLayer(), outputStream);
    writeTextImageLayer(map.textLayer(), outputStream);

    // Checksum
    ByteArrayOutputStream checksumBuilder = new ByteArrayOutputStream();
    writeSignature(checksumBuilder);
    writeField(RkmField.MAP_CODE_NAME, map.codename().getBytes(StandardCharsets.UTF_8), checksumBuilder);
    writeField(RkmField.MAP_DISPLAY_NAME, map.displayName().getBytes(StandardCharsets.UTF_8), checksumBuilder);
    writeField(RkmField.MAP_AUTHOR_NAME, map.author().getBytes(StandardCharsets.UTF_8), checksumBuilder);

    writeVertices(map.vertices().toArray(new Territory[0]), checksumBuilder);
    writeEdges(map.edges().toArray(new Border[0]), checksumBuilder);

    writeBaseImageLayer(map.baseLayer(), checksumBuilder);
    writeTextImageLayer(map.textLayer(), checksumBuilder);

    writeField(RkmField.CHECKSUM, MessageDigest.getInstance("SHA-512").digest(checksumBuilder.toByteArray()), outputStream);
    if (shouldCloseStream) {
      outputStream.close();
    }
  }

  private void writeSignature(OutputStream outputStream) throws IOException {
    outputStream.write(signature); // 8 bytes
  }

  private void writeField(RkmField field, byte[] data, OutputStream outputStream) throws IOException {
    if (data.length < 1) {
      throw new IllegalArgumentException("Invalid field length of " + data.length + ": field length must be greater than or equal to 1.");
    }
    outputStream.write(field.fieldName()); // 4 bytes
    outputStream.write(ByteBuffer.allocate(4).putInt(data.length).array()); // 4 bytes
    outputStream.write(data); // fieldLength bytes
  }

  private void writeVertices(Territory[] vertices, OutputStream outputStream) throws IOException {
    outputStream.write(RkmField.VERTICES.fieldName()); // 4 bytes

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    for (Territory vertex : vertices) {
      writeVertex(vertex, bos);
    }
    byte[] verticesByteArray = bos.toByteArray();
    bos.close();

    outputStream.write(ByteBuffer.allocate(4).putInt(4 + verticesByteArray.length).array()); // 4 bytes
    outputStream.write(ByteBuffer.allocate(4).putInt(vertices.length).array()); // 4 bytes
    outputStream.write(verticesByteArray);
  }

  private void writeVertex(Territory vertex, OutputStream outputStream) throws IOException {
    outputStream.write(ByteBuffer.allocate(4).putInt(vertex.identity().toString().length()).array()); // 4 bytes
    outputStream.write(vertex.identity().toString().getBytes(StandardCharsets.UTF_8));
    outputStream.write(ByteBuffer.allocate(4).putInt(vertex.nuclei().size()).array()); // 4 bytes
    for (Nucleus n : vertex.nuclei()) {
      outputStream.write(ByteBuffer.allocate(4).putInt(n.x()).array());
      outputStream.write(ByteBuffer.allocate(4).putInt(n.y()).array());
    }
  }

  private void writeEdges(Border[] edges, OutputStream outputStream) throws IOException {
    outputStream.write(RkmField.EDGES.fieldName()); // 4 bytes

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    for (Border edge : edges) {
      writeEdge(edge, bos);
    }
    byte[] edgesByteArray = bos.toByteArray();
    bos.close();

    outputStream.write(ByteBuffer.allocate(4).putInt(4 + edgesByteArray.length).array()); // 4 bytes
    outputStream.write(ByteBuffer.allocate(4).putInt(edges.length).array()); // 4 bytes
    outputStream.write(edgesByteArray);
  }

  private void writeEdge(Border edge, OutputStream outputStream) throws IOException {
    outputStream.write(ByteBuffer.allocate(4).putInt(edge.source().toString().length()).array()); // 4 bytes
    outputStream.write(edge.source().toString().getBytes(StandardCharsets.UTF_8));
    outputStream.write(ByteBuffer.allocate(4).putInt(edge.target().toString().length()).array()); // 4 bytes
    outputStream.write(edge.target().toString().getBytes(StandardCharsets.UTF_8));
  }

  private void writeBaseImageLayer(BufferedImage image, OutputStream outputStream) throws IOException {
    outputStream.write(RkmField.MAP_IMAGE_BASE.fieldName()); // 4 bytes

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ImageIO.write(image, "png", bos);
    byte[] imageData = bos.toByteArray();
    bos.close();

    outputStream.write(ByteBuffer.allocate(4).putInt(imageData.length).array()); // 4 bytes
    outputStream.write(imageData); // fieldLength bytes
  }

  private void writeTextImageLayer(BufferedImage image, OutputStream outputStream) throws IOException {
    outputStream.write(RkmField.MAP_IMAGE_TEXT.fieldName()); // 4 bytes

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    ImageIO.write(image, "png", bos);
    byte[] imageData = bos.toByteArray();
    bos.close();

    outputStream.write(ByteBuffer.allocate(4).putInt(imageData.length).array()); // 4 bytes
    outputStream.write(imageData); // fieldLength bytes
  }

}
