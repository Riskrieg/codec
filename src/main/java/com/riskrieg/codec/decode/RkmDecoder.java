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

package com.riskrieg.codec.decode;

import com.riskrieg.codec.RkmField;
import com.riskrieg.map.RkmMap;
import com.riskrieg.map.Territory;
import com.riskrieg.map.territory.Border;
import com.riskrieg.map.territory.Nucleus;
import com.riskrieg.map.territory.TerritoryIdentity;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.imageio.ImageIO;

public final class RkmDecoder implements Decoder<RkmMap> {

  // 8B: File signature. HEX: 83 52 4B 4D 0D 0A 1A 0A -- \131 R K M \r \n \032 \n
  private final byte[] signature = new byte[]{(byte) 0x83, (byte) 0x52, (byte) 0x4B, (byte) 0x4D, (byte) 0x0D, (byte) 0x0A, (byte) 0x1A, (byte) 0x0A};

  @Override
  public RkmMap decode(Path path) throws IOException, NoSuchAlgorithmException {
    Objects.requireNonNull(path);
    final ByteArrayInputStream input = new ByteArrayInputStream(Files.readAllBytes(path));
    final long lengthInBytes = path.toFile().length();
    RkmMap result = decodeInternal(input, lengthInBytes);
    input.close();
    return result;
  }

  @Override
  public RkmMap decode(URL url) throws IOException, NoSuchAlgorithmException {
    Objects.requireNonNull(url);
    try (InputStream inputStream = url.openStream()) {
      final byte[] fileData = inputStream.readAllBytes();
      final ByteArrayInputStream input = new ByteArrayInputStream(fileData);
      final long lengthInBytes = fileData.length;
      RkmMap result = decodeInternal(input, lengthInBytes);
      input.close();
      return result;
    }
  }

  @Override
  public RkmMap decode(byte[] data) throws IOException, NoSuchAlgorithmException {
    Objects.requireNonNull(data);
    final ByteArrayInputStream input = new ByteArrayInputStream(data);
    final long lengthInBytes = data.length;
    RkmMap result = decodeInternal(input, lengthInBytes);
    input.close();
    return result;
  }

  private RkmMap decodeInternal(ByteArrayInputStream input, long lengthInBytes) throws IOException, NoSuchAlgorithmException {
    if (lengthInBytes < signature.length + 4 + 4 + 64) { // Check length first before reading
      throw new IllegalStateException("file length is too short to be a valid .rkm file");
    }
    if (!checkSignature(input)) {
      throw new IllegalStateException("file signature is invalid");
    }
    ByteArrayOutputStream checksumBuilder = new ByteArrayOutputStream(); // Used to validate the checksum.
    checksumBuilder.write(readSignature(input));

    RkmMapBuilder builder = new RkmMapBuilder();

    boolean eof = false;
    while (!eof) {
      byte[] fieldNameByteArray = readFieldName(input);
      byte[] fieldLengthByteArray = readFieldLength(input);

      int fieldLength = ByteBuffer.wrap(fieldLengthByteArray).getInt();
      long remainingBytes = lengthInBytes - checksumBuilder.size() - fieldNameByteArray.length - fieldLengthByteArray.length;

      if (fieldLength < 1) {
        throw new IllegalArgumentException("field length cannot be negative or zero");
      } else if (fieldLength > remainingBytes) {
        throw new IllegalArgumentException("field length is longer than the remaining bytes in the file");
      }

      byte[] fieldDataByteArray = readFieldData(fieldLength, input);

      switch (RkmField.of(fieldNameByteArray)) {
        case UNKNOWN -> {
          // Do nothing, skipped over.
        }
        case MAP_CODE_NAME -> builder.setCodename(new String(fieldDataByteArray, StandardCharsets.UTF_8));
        case MAP_DISPLAY_NAME -> builder.setDisplayName(new String(fieldDataByteArray, StandardCharsets.UTF_8));
        case MAP_AUTHOR_NAME -> builder.setAuthor(new String(fieldDataByteArray, StandardCharsets.UTF_8));
        case VERTICES -> builder.setVertices(decodeVertices(fieldDataByteArray));
        case EDGES -> builder.setEdges(decodeEdges(fieldDataByteArray));
        case MAP_IMAGE_BASE -> builder.setBaseLayer(decodeImage(fieldDataByteArray));
        case MAP_IMAGE_TEXT -> builder.setTextLayer(decodeImage(fieldDataByteArray));
        case CHECKSUM -> {
          eof = true;
          byte[] checksum = MessageDigest.getInstance("SHA-512").digest(checksumBuilder.toByteArray());
          if (!Arrays.equals(checksum, fieldDataByteArray)) {
            throw new IllegalStateException("invalid checksum");
          }
        }
      }

      if (!eof) {
        checksumBuilder.write(fieldNameByteArray);
        checksumBuilder.write(fieldLengthByteArray);
        checksumBuilder.write(fieldDataByteArray);
      }

    }
    input.reset();
    return builder.build();
  }

  private boolean checkSignature(ByteArrayInputStream bis) {
    try {
      bis.mark(0);
      byte[] testSignature = readSignature(bis);
      bis.reset();
      return Arrays.equals(testSignature, signature);
    } catch (IOException e) {
      return false;
    }
  }

  private byte[] readSignature(ByteArrayInputStream bis) throws IOException {
    return bis.readNBytes(8);
  }

  private byte[] readFieldName(ByteArrayInputStream bis) throws IOException {
    return bis.readNBytes(4);
  }

  private byte[] readFieldLength(ByteArrayInputStream bis) throws IOException {
    return bis.readNBytes(4);
  }

  private byte[] readFieldData(int length, ByteArrayInputStream bis) throws IOException {
    return bis.readNBytes(length);
  }

  private Set<Territory> decodeVertices(byte[] data) throws IOException {
    // Vertex Format: [string-id-length-in-bytes][string-id][number-of-nuclei][x1][y1][x2][y2]...[xN][yN]
    ByteArrayInputStream bis = new ByteArrayInputStream(data);
    Set<Territory> result = new HashSet<>();

    // Make sure to read the number of vertices first before decoding based on the vertex format!
    int vertexCount = ByteBuffer.wrap(bis.readNBytes(4)).getInt();
    for (int v = 0; v < vertexCount; v++) {
      int vertexIdLengthInBytes = ByteBuffer.wrap(bis.readNBytes(4)).getInt();
      String id = new String(bis.readNBytes(vertexIdLengthInBytes), StandardCharsets.UTF_8);

      int nucleusCount = ByteBuffer.wrap(bis.readNBytes(4)).getInt();
      Set<Nucleus> nuclei = new HashSet<>();
      for (int n = 0; n < nucleusCount; n++) {
        int x = ByteBuffer.wrap(bis.readNBytes(4)).getInt();
        int y = ByteBuffer.wrap(bis.readNBytes(4)).getInt();
        nuclei.add(new Nucleus(x, y));
      }
      result.add(new Territory(new TerritoryIdentity(id), nuclei));
    }

    return result;
  }

  private Set<Border> decodeEdges(byte[] data) throws IOException {
    ByteArrayInputStream bis = new ByteArrayInputStream(data);
    Set<Border> result = new HashSet<>();

    int edgeCount = ByteBuffer.wrap(bis.readNBytes(4)).getInt();
    for (int e = 0; e < edgeCount; e++) {
      int sourceIdLengthInBytes = ByteBuffer.wrap(bis.readNBytes(4)).getInt();
      String sourceId = new String(bis.readNBytes(sourceIdLengthInBytes), StandardCharsets.UTF_8);
      int targetIdLengthInBytes = ByteBuffer.wrap(bis.readNBytes(4)).getInt();
      String targetId = new String(bis.readNBytes(targetIdLengthInBytes), StandardCharsets.UTF_8);
      result.add(new Border(new TerritoryIdentity(sourceId), new TerritoryIdentity(targetId)));
    }

    return result;
  }

  private BufferedImage decodeImage(byte[] data) throws IOException {
    return ImageIO.read(new ByteArrayInputStream(data));
  }

}

final class RkmMapBuilder {

  private String codename;
  private String displayName;
  private String author;
  private Set<Territory> vertices;
  private Set<Border> edges;
  private BufferedImage baseLayer;
  private BufferedImage textLayer;

  public RkmMapBuilder setCodename(String codename) {
    this.codename = codename;
    return this;
  }

  public RkmMapBuilder setDisplayName(String displayName) {
    this.displayName = displayName;
    return this;
  }

  public RkmMapBuilder setAuthor(String author) {
    this.author = author;
    return this;
  }

  public RkmMapBuilder setVertices(Set<Territory> vertices) {
    this.vertices = vertices;
    return this;
  }

  public RkmMapBuilder setEdges(Set<Border> edges) {
    this.edges = edges;
    return this;
  }

  public RkmMapBuilder setBaseLayer(BufferedImage baseLayer) {
    this.baseLayer = baseLayer;
    return this;
  }

  public RkmMapBuilder setTextLayer(BufferedImage textLayer) {
    this.textLayer = textLayer;
    return this;
  }

  public RkmMap build() {
    return new RkmMap(codename, displayName, author, vertices, edges, baseLayer, textLayer);
  }


}
