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
package org.sonar.lua.checks;

import com.sonar.sslr.api.AstNode;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Context for Lua checks providing file information and issue reporting.
 */
public interface LuaCheckContext {

  /**
   * Get the file being analyzed.
   */
  File getFile();

  /**
   * Get the charset of the file.
   */
  Charset getCharset();

  /**
   * Read lines from the file.
   */
  List<String> readLines();

  /**
   * Get the parsed AST root node.
   */
  AstNode getRootNode();

  /**
   * Add a file-level issue.
   */
  void addIssue(LuaCheck check, String message);

  /**
   * Add an issue at a specific line.
   */
  void addIssue(LuaCheck check, int line, String message);

  /**
   * Add an issue with remediation cost.
   */
  void addIssueWithCost(LuaCheck check, int line, String message, double cost);
}
