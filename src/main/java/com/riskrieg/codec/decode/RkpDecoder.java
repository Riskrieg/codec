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
    return JsonHelper.read(path, RkpPalette.class);
  }

  @Override
  public RkpPalette decode(URL url) throws IOException {
    Objects.requireNonNull(url);
    try (InputStream inputStream = url.openStream()) {
      final byte[] data = inputStream.readAllBytes();
      return JsonHelper.read(new String(data, StandardCharsets.UTF_8), RkpPalette.class);
    }
  }

  @Override
  public RkpPalette decode(byte[] data) throws IOException {
    Objects.requireNonNull(data);
    return JsonHelper.read(new String(data, StandardCharsets.UTF_8), RkpPalette.class);
  }

}
