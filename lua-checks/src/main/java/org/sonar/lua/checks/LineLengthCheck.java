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
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;

import java.util.List;

/**
 * Check that lines are not too long.
 */
@Rule(
  key = "LineLength",
  name = "Lines should not be too long",
  description = "Checks that lines do not exceed a configurable length.")
public class LineLengthCheck extends LuaCheck {

  private static final int DEFAULT_MAXIMUM_LINE_LENGTH = 80;

  @RuleProperty(
    key = "maximumLineLength",
    description = "The maximum authorized line length.",
    defaultValue = "" + DEFAULT_MAXIMUM_LINE_LENGTH)
  public int maximumLineLength = DEFAULT_MAXIMUM_LINE_LENGTH;

  @Override
  public void visitFile(AstNode astNode) {
    List<String> lines = readLines();
    for (int i = 0; i < lines.size(); i++) {
      String line = lines.get(i);
      if (line.length() > maximumLineLength) {
        addLineIssue(i + 1, "Split this {0} characters long line (which is greater than {1} authorized).",
            line.length(), maximumLineLength);
      }
    }
  }

  public int getMaximumLineLength() {
    return maximumLineLength;
  }
}
