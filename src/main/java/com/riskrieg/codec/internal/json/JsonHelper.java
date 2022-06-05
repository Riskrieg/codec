/*
 *     Riskrieg, an open-source conflict simulation game.
 *     Copyright (C) 2022 Aaron Yoder <aaronjyoder@gmail.com> and the Riskrieg contributors
 *
 *     This code is licensed under the MIT license.
 */

package com.riskrieg.codec.internal.json;

import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class JsonHelper {

  private JsonHelper() {
  }

  private static JsonMapper.Builder jsonAdapterBuilder = JsonMapper.builder().addModule(new JavaTimeModule());

  public static void registerIfBaseType(final Class<?>... baseClasses) {
    var builder = BasicPolymorphicTypeValidator.builder();
    for (Class<?> baseClass : baseClasses) {
      builder.allowIfBaseType(baseClass);
    }
    jsonAdapterBuilder = jsonAdapterBuilder.activateDefaultTypingAsProperty(builder.build(), DefaultTyping.NON_FINAL, "type");
  }

  public static void registerIfSubType(final Class<?>... subClasses) {
    var builder = BasicPolymorphicTypeValidator.builder();
    for (Class<?> subClass : subClasses) {
      builder.allowIfSubType(subClass);
    }
    jsonAdapterBuilder = jsonAdapterBuilder.activateDefaultTypingAsProperty(builder.build(), DefaultTyping.NON_FINAL, "type");
  }

  private static JsonMapper jsonAdapter() {
    return jsonAdapterBuilder.build();
  }

  // Read

  @Nullable
  public static <T> T read(@NonNull Path path, @NonNull Class<T> type) throws IOException {
    if (Files.isRegularFile(path) && Files.isReadable(path)) {
      return jsonAdapter().readValue(Files.newBufferedReader(path), jsonAdapter().constructType(type));
    }
    return null;
  }

  @Nullable
  public static <T> T read(@NonNull Path path, @NonNull Type type) throws IOException {
    if (Files.isRegularFile(path) && Files.isReadable(path)) {
      return jsonAdapter().readValue(Files.newBufferedReader(path), jsonAdapter().constructType(type));
    }
    return null;
  }

  @Nullable
  public static <T> T read(@NonNull String string, @NonNull Type type) throws IOException {
    return jsonAdapter().readValue(string, jsonAdapter().constructType(type));
  }

  // Write

  public static <T> void write(@NonNull Path path, @NonNull Class<T> type, @NonNull T object) throws IOException {
    Files.createDirectories(path.getParent());
    Files.writeString(path, jsonAdapter().writerWithDefaultPrettyPrinter().writeValueAsString(object), StandardCharsets.UTF_8);
  }

  public static <T> void write(@NonNull Path path, @NonNull Type type, @NonNull T object) throws IOException {
    Files.createDirectories(path.getParent());
    Files.writeString(path, jsonAdapter().writerWithDefaultPrettyPrinter().writeValueAsString(object), StandardCharsets.UTF_8);
  }

  public static <T> void write(@NonNull OutputStream outputStream, @NonNull Type type, @NonNull T object) throws IOException {
    outputStream.write(jsonAdapter().writerWithDefaultPrettyPrinter().writeValueAsString(object).getBytes(StandardCharsets.UTF_8));
  }

}
