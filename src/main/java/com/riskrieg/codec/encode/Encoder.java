/*
 *     Riskrieg, an open-source conflict simulation game.
 *     Copyright (C) 2021-2022 Aaron Yoder <aaronjyoder@gmail.com> and the Riskrieg contributors
 *
 *     This code is licensed under the MIT license.
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
