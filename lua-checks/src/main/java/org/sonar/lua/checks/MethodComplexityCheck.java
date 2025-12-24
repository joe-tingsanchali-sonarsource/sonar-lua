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
 * Check that methods (named functions) are not too complex.
 */
@Rule(key = "MethodComplexity")
public class MethodComplexityCheck extends LuaCheck {

  private static final int DEFAULT_MAXIMUM_METHOD_COMPLEXITY_THRESHOLD = 10;

  @RuleProperty(
    key = "maximumMethodComplexityThreshold",
    description = "The maximum authorized complexity.",
    defaultValue = "" + DEFAULT_MAXIMUM_METHOD_COMPLEXITY_THRESHOLD)
  public int maximumMethodComplexityThreshold = DEFAULT_MAXIMUM_METHOD_COMPLEXITY_THRESHOLD;

  @Override
  public void init() {
    subscribeTo(LuaGrammar.FUNCSTAT);
  }

  @Override
  public void leaveNode(AstNode node) {
    int complexity = calculateComplexity(node);
    if (complexity > maximumMethodComplexityThreshold) {
      String message = String.format(
          "Method has a complexity of %d which is greater than %d authorized.",
          complexity, maximumMethodComplexityThreshold);
      addIssueWithCost(node, message, (double) complexity - maximumMethodComplexityThreshold);
    }
  }

  private int calculateComplexity(AstNode funcStatNode) {
    AstNode funcBody = funcStatNode.getFirstChild(LuaGrammar.FUNCBODY);
    if (funcBody != null) {
      AstNode block = funcBody.getFirstChild(LuaGrammar.BLOCK);
      if (block != null) {
        return 1 + ComplexityCalculator.calculate(block);
      }
    }
    return 1;
  }

  public void setMaximumMethodComplexityThreshold(int threshold) {
    this.maximumMethodComplexityThreshold = threshold;
  }
}
