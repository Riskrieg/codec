/*
 *     Riskrieg, an open-source conflict simulation game.
 *     Copyright (C) 2021-2022 Aaron Yoder <aaronjyoder@gmail.com> and the Riskrieg contributors
 *
 *     This code is licensed under the MIT license.
 */

package com.riskrieg.codec.decode;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;

public sealed interface Decoder<T> permits RkmDecoder, RkpDecoder {

  T decode(Path path) throws IOException, NoSuchAlgorithmException;

  T decode(URL url) throws IOException, NoSuchAlgorithmException;

  T decode(byte[] data) throws IOException, NoSuchAlgorithmException;

}
