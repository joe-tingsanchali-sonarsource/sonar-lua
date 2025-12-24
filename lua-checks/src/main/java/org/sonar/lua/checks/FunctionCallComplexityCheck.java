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
 * Check that function calls are not too complex.
 */
@Rule(
  key = "FuncCaLL",
  name = "Function calls should not be too complex",
  description = "Checks that function call complexity does not exceed a configurable threshold.")
public class FunctionCallComplexityCheck extends LuaCheck {

  private static final int DEFAULT_MAXIMUM_FUNCCALL_COMPLEXITY_THRESHOLD = 5;

  @RuleProperty(
    key = "maxFuncCallComplexityThreshold",
    description = "The maximum authorized call complexity.",
    defaultValue = "" + DEFAULT_MAXIMUM_FUNCCALL_COMPLEXITY_THRESHOLD)
  public int maximumFunctionCallComplexityThreshold = DEFAULT_MAXIMUM_FUNCCALL_COMPLEXITY_THRESHOLD;

  @Override
  public void init() {
    subscribeTo(LuaGrammar.FUNCTIONCALL);
  }

  @Override
  public void leaveNode(AstNode node) {
    int complexity = ComplexityCalculator.calculate(node);
    if (complexity > maximumFunctionCallComplexityThreshold) {
      String message = String.format(
          "Function call has a complexity of %d which is greater than %d authorized.",
          complexity, maximumFunctionCallComplexityThreshold);
      addIssueWithCost(node, message, (double) complexity - maximumFunctionCallComplexityThreshold);
    }
  }

  public void setMaximumFunctionCallComplexityThreshold(int threshold) {
    this.maximumFunctionCallComplexityThreshold = threshold;
  }
}
