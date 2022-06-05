/*
 *     Riskrieg, an open-source conflict simulation game.
 *     Copyright (C) 2021-2022 Aaron Yoder <aaronjyoder@gmail.com> and the Riskrieg contributors
 *
 *     This code is licensed under the MIT license.
 */

package com.riskrieg.codec;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

public enum RkmField {

  UNKNOWN("UNKN"),
  /**
   * The code name of the map. Must have only lowercase alphanumeric characters with no spaces. Hyphens are allowed, but it cannot start and end with a hyphen, and cannot be only
   * hypens.
   * <p>
   * <b>Regex</b>: {@value com.riskrieg.map.RkmMap#CODENAME_REGEX}
   * <p>
   * <b>Field name</b>: MCNM
   * <p>
   * <b>Usage</b>: MCNM[length-in-bytes][string-as-bytes]
   */
  MAP_CODE_NAME("MCNM"),

  /**
   * The display name of the map. Can be any non-blank string.
   * <p>
   * <b>Field name</b>: MDNM
   * <p>
   * <b>Usage</b>: MDNM[length-in-bytes][string-as-bytes]
   */
  MAP_DISPLAY_NAME("MDNM"),

  /**
   * The name of the author of the map. Can be any non-blank string.
   * <p>
   * <b>Field name</b>: MATN
   * <p>
   * <b>Usage</b>: MATN[length-in-bytes][string-as-bytes]
   */
  MAP_AUTHOR_NAME("MATN"),

  /**
   * The list of (x, y) coordinates that define the vertices of the map graph. Each (x, y) pair is considered a 'nucleus', and each vertex can have multiple nuclei.
   * <p>
   * <b>Field name</b>: VTCS
   * <p>
   * <b>Usage</b>: VTCS[length-in-bytes][number-of-vertices-as-int][vertex1][vertex2]...[vertexN]
   * <p>
   * <b>Vertex Format</b>: [string-id-length-in-bytes][string-id][number-of-nuclei][x1][y1][x2][y2]...[xN][yN]
   */
  VERTICES("VTCS"),

  /**
   * The list of vertex ID pairs that define all the connections between vertices. Each edge defines a single connection between two different vertices.
   * <p>
   * <b>Field name</b>: EDGS
   * <p>
   * <b>Usage</b>: EDGS[length-in-bytes][number-of-edges-as-int][edge1][edge2]...[edgeN]
   * <p>
   * <b>Edge Format</b>: [source-string-id-length-in-bytes][source-string-id][target-string-id-length-in-bytes][target-string-id]
   */
  EDGES("EDGS"),

  /**
   * The PNG data that defines the base layer of the map image.
   * <p>
   * <b>Field name</b>: MIBS
   * <p>
   * <b>Usage</b>: MIBS[length-in-bytes][image-encoded-as-png-bytes]
   */
  MAP_IMAGE_BASE("MIBS"),

  /**
   * The PNG data that defines the text layer of the map image.
   * <p>
   * <b>Field name</b>: MITX
   * <p>
   * <b>Usage</b>: MITX[length-in-bytes][image-encoded-as-png-bytes]
   */
  MAP_IMAGE_TEXT("MITX"),

  /**
   * The file checksum that covers everything in the file from the very beginning all the way until the point the checksum is written. This is also considered an end-of-file
   * marker.
   * <p>
   * <b>Field name</b>: CKSM
   * <p>
   * <b>Usage</b>: CKSM[length-in-bytes][checksum-data-as-bytes]
   */
  CHECKSUM("CKSM");

  private final byte[] fieldName;

  RkmField(String fieldName) {
    Objects.requireNonNull(fieldName);
    if (fieldName.getBytes(StandardCharsets.UTF_8).length != 4) {
      throw new IllegalArgumentException("String 'fieldName' length must be exactly four (4) bytes");
    }
    this.fieldName = fieldName.getBytes(StandardCharsets.UTF_8);
  }

  public byte[] fieldName() {
    return fieldName;
  }

  public static RkmField of(byte[] fieldNameByteArray) {
    Objects.requireNonNull(fieldNameByteArray);
    for (RkmField field : RkmField.values()) {
      if (Arrays.equals(field.fieldName(), fieldNameByteArray)) {
        return field;
      }
    }
    return UNKNOWN;
  }

}
