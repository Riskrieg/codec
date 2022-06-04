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

package com.riskrieg.codec.encode;

import com.riskrieg.codec.internal.json.JsonHelper;
import com.riskrieg.palette.RkpPalette;
import java.io.IOException;
import java.io.OutputStream;

public final class RkpEncoder implements Encoder<RkpPalette> {

  @Override
  public void encode(RkpPalette object, OutputStream outputStream, boolean shouldCloseStream) throws IOException {
    JsonHelper.write(outputStream, RkpPalette.class, object);
    if (shouldCloseStream) {
      outputStream.close();
    }
  }

}
