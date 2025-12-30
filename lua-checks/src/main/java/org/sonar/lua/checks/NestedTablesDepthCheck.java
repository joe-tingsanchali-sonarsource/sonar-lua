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
import org.sonar.lua.grammar.LuaGrammar;

/**
 * Check that tables are not nested too deeply.
 */
@Rule(
  key = "NestedTable",
  name = "Tables should not be nested too deeply",
  description = "Checks that tables are not nested too deeply.",
  tags = {Tags.BRAIN_OVERLOAD})
public class NestedTablesDepthCheck extends LuaCheck {

  private int nestingLevel;
  private static final int DEFAULT_MAX = 3;

  @RuleProperty(
    key = "max",
    description = "Maximum allowed table nesting depth.",
    defaultValue = "" + DEFAULT_MAX)
  public int max = DEFAULT_MAX;

  @Override
  public void init() {
    subscribeTo(LuaGrammar.TABLECONSTRUCTOR);
  }

  @Override
  public void visitFile(AstNode astNode) {
    nestingLevel = 0;
  }

  @Override
  public void visitNode(AstNode astNode) {
    nestingLevel++;
    if (nestingLevel == max + 1) {
      addIssue(astNode, "Refactor this code to not nest more than {0} table(s).", max);
    }
  }

  @Override
  public void leaveNode(AstNode astNode) {
    nestingLevel--;
  }

  public int getMax() {
    return max;
  }
}
