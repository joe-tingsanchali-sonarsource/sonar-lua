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

/**
 * Check that files don't have too many lines.
 */
@Rule(key = TooManyLinesInFileCheck.CHECK_KEY)
public class TooManyLinesInFileCheck extends LuaCheck {

  public static final String CHECK_KEY = "S104";
  private static final int DEFAULT = 1000;

  @RuleProperty(
    key = "maximum",
    description = "The maximum authorized lines in a file.",
    defaultValue = "" + DEFAULT)
  public int maximum = DEFAULT;

  @Override
  public void leaveFile(AstNode astNode) {
    int lines = readLines().size();
    if (lines > maximum) {
      addFileIssue("File \"{0}\" has {1} lines, which is greater than {2} authorized. Split it into smaller files.",
          getFile().getName(), lines, maximum);
    }
  }
}
