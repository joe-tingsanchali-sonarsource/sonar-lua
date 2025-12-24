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
import org.sonar.lua.grammar.LuaGrammar;

/**
 * Check that local functions are not too complex.
 */
@Rule(
  key = "LocalFunctionComplexity",
  name = "Local functions should not be too complex",
  description = "Checks that local function complexity does not exceed a configurable threshold.")
public class LocalFunctionComplexityCheck extends LuaCheck {

  private static final int DEFAULT_MAXIMUM_FUNCTION_COMPLEXITY_THRESHOLD = 10;

  @RuleProperty(
    key = "maximumFunctionComplexityThreshold",
    description = "The maximum authorized complexity.",
    defaultValue = "" + DEFAULT_MAXIMUM_FUNCTION_COMPLEXITY_THRESHOLD)
  public int maximumFunctionComplexityThreshold = DEFAULT_MAXIMUM_FUNCTION_COMPLEXITY_THRESHOLD;

  @Override
  public void init() {
    subscribeTo(LuaGrammar.LOCALFUNCSTAT);
  }

  @Override
  public void leaveNode(AstNode node) {
    int complexity = calculateComplexity(node);
    if (complexity > maximumFunctionComplexityThreshold) {
      String message = String.format(
          "Local function has a complexity of %d which is greater than %d authorized.",
          complexity, maximumFunctionComplexityThreshold);
      addIssueWithCost(node, message, (double) complexity - maximumFunctionComplexityThreshold);
    }
  }

  private int calculateComplexity(AstNode localFuncNode) {
    AstNode funcBody = localFuncNode.getFirstChild(LuaGrammar.FUNCBODY);
    if (funcBody != null) {
      AstNode block = funcBody.getFirstChild(LuaGrammar.BLOCK);
      if (block != null) {
        return 1 + ComplexityCalculator.calculate(block);
      }
    }
    return 1;
  }

  public void setMaximumFunctionComplexityThreshold(int threshold) {
    this.maximumFunctionComplexityThreshold = threshold;
  }
}
