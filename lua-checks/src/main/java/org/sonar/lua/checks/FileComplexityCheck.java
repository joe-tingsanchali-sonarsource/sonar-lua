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
import org.sonar.lua.checks.utils.ComplexityCalculator;

/**
 * Check that files are not too complex.
 */
@Rule(
  key = FileComplexityCheck.CHECK_KEY,
  name = "Files should not be too complex",
  description = "Checks that file complexity does not exceed a configurable threshold.")
public class FileComplexityCheck extends LuaCheck {

  public static final String CHECK_KEY = "FileComplexity";
  private static final int DEFAULT_MAXIMUM_FILE_COMPLEXITY_THRESHOLD = 200;

  @RuleProperty(
    key = "maximumFileComplexityThreshold",
    description = "The maximum authorized file complexity.",
    defaultValue = "" + DEFAULT_MAXIMUM_FILE_COMPLEXITY_THRESHOLD)
  public int maximumFileComplexityThreshold = DEFAULT_MAXIMUM_FILE_COMPLEXITY_THRESHOLD;

  @Override
  public void leaveFile(AstNode astNode) {
    int complexity = ComplexityCalculator.calculateFileComplexity(astNode);
    if (complexity > maximumFileComplexityThreshold) {
      addFileIssue("File has a complexity of {0} which is greater than {1} authorized.",
          complexity, maximumFileComplexityThreshold);
    }
  }

  public void setMaximumFileComplexityThreshold(int threshold) {
    this.maximumFileComplexityThreshold = threshold;
  }
}
