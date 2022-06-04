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

import java.io.IOException;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;

public sealed interface Encoder<T> permits RkmEncoder, RkpEncoder {

  void encode(T object, OutputStream outputStream, boolean shouldCloseStream) throws IOException, NoSuchAlgorithmException;

  default void encode(T object, OutputStream outputStream) throws IOException, NoSuchAlgorithmException {
    encode(object, outputStream, true);
  }

}
