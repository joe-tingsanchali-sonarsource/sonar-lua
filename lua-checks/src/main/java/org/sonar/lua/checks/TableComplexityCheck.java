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
import org.sonar.lua.checks.utils.Tags;
import org.sonar.check.RuleProperty;
import org.sonar.lua.checks.utils.ComplexityCalculator;
import org.sonar.lua.grammar.LuaGrammar;

/**
 * Check that tables are not too complex.
 */
@Rule(
  key = "TableComplexity",
  name = "Tables should not be too complex",
  description = "Checks that table complexity does not exceed a configurable threshold.",
  tags = {Tags.BRAIN_OVERLOAD})
public class TableComplexityCheck extends LuaCheck {

  private static final int DEFAULT_MAXIMUM_TABLE_COMPLEXITY_THRESHOLD = 10;

  @RuleProperty(
    key = "maximumTableComplexityThreshold",
    description = "The maximum authorized complexity.",
    defaultValue = "" + DEFAULT_MAXIMUM_TABLE_COMPLEXITY_THRESHOLD)
  public int maximumTableComplexityThreshold = DEFAULT_MAXIMUM_TABLE_COMPLEXITY_THRESHOLD;

  @Override
  public void init() {
    subscribeTo(LuaGrammar.TABLECONSTRUCTOR);
  }

  @Override
  public void leaveNode(AstNode node) {
    int complexity = ComplexityCalculator.calculate(node);
    if (complexity > maximumTableComplexityThreshold) {
      String message = String.format(
          "Table has a complexity of %d which is greater than %d authorized.",
          complexity, maximumTableComplexityThreshold);
      addIssueWithCost(node, message, (double) complexity - maximumTableComplexityThreshold);
    }
  }

  public void setMaximumTableComplexityThreshold(int threshold) {
    this.maximumTableComplexityThreshold = threshold;
  }
}
