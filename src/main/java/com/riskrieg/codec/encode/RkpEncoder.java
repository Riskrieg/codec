/*
 *     Riskrieg, an open-source conflict simulation game.
 *     Copyright (C) 2022 Aaron Yoder <aaronjyoder@gmail.com> and the Riskrieg contributors
 *
 *     This code is licensed under the MIT license.
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
