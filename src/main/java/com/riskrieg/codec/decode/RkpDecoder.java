/*
 *     Riskrieg, an open-source conflict simulation game.
 *     Copyright (C) 2022 Aaron Yoder <aaronjyoder@gmail.com> and the Riskrieg contributors
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

import com.riskrieg.codec.internal.json.JsonHelper;
import com.riskrieg.palette.RkpPalette;
import com.riskrieg.palette.legacy.LegacyPalette;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Objects;

public final class RkpDecoder implements Decoder<RkpPalette> {

  @Override
  public RkpPalette decode(Path path) throws IOException {
    Objects.requireNonNull(path);
    try {
      return JsonHelper.read(path, RkpPalette.class);
    } catch (Exception e) {
      return decodeLegacy(path);
    }
  }

  private RkpPalette decodeLegacy(Path path) throws IOException {
    try {
      return JsonHelper.read(path, LegacyPalette.class).toRkpPalette();
    } catch (NullPointerException e) {
      return null;
    }
  }

  @Override
  public RkpPalette decode(URL url) throws IOException {
    Objects.requireNonNull(url);
    try {
      try (InputStream inputStream = url.openStream()) {
        final byte[] data = inputStream.readAllBytes();
        return JsonHelper.read(new String(data, StandardCharsets.UTF_8), RkpPalette.class);
      }
    } catch (Exception e) {
      return decodeLegacy(url);
    }
  }

  private RkpPalette decodeLegacy(URL url) throws IOException {
    try (InputStream inputStream = url.openStream()) {
      final byte[] data = inputStream.readAllBytes();
      LegacyPalette legacy = JsonHelper.read(new String(data, StandardCharsets.UTF_8), LegacyPalette.class);
      return legacy.toRkpPalette();
    } catch (NullPointerException e) {
      return null;
    }
  }

  @Override
  public RkpPalette decode(byte[] data) throws IOException {
    Objects.requireNonNull(data);
    try {
      return JsonHelper.read(new String(data, StandardCharsets.UTF_8), RkpPalette.class);
    } catch (Exception e) {
      return decodeLegacy(data);
    }
  }

  private RkpPalette decodeLegacy(byte[] data) throws IOException {
    try {
      LegacyPalette legacy = JsonHelper.read(new String(data, StandardCharsets.UTF_8), LegacyPalette.class);
      return legacy.toRkpPalette();
    } catch (NullPointerException e) {
      return null;
    }
  }

}
