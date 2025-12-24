/*
 * SonarQube Lua Plugin
 * Copyright (C) 2013-2024
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */
package org.sonar.lua.parser;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.impl.Parser;
import org.sonar.lua.grammar.LuaGrammar;
import org.sonar.sslr.parser.LexerlessGrammar;
import org.sonar.sslr.parser.ParserAdapter;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;

/**
 * Lua Parser using SSLR
 */
public class LuaParser {

  private final Parser<LexerlessGrammar> parser;
  private final Charset charset;

  public LuaParser(Charset charset) {
    this.charset = charset;
    this.parser = new ParserAdapter<>(charset, LuaGrammar.createGrammar());
  }

  public AstNode parse(File file) {
    try {
      String content = new String(Files.readAllBytes(file.toPath()), charset);
      return parse(content);
    } catch (Exception e) {
      throw new LuaParseException("Failed to parse file: " + file.getAbsolutePath(), e);
    }
  }

  public AstNode parse(String source) {
    try {
      return parser.parse(source);
    } catch (Exception e) {
      throw new LuaParseException("Failed to parse Lua source", e);
    }
  }

  public Charset getCharset() {
    return charset;
  }

  public static class LuaParseException extends RuntimeException {
    public LuaParseException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
