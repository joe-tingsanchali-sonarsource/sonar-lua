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
 * Check that functions don't have too many parameters.
 */
@Rule(
  key = "S107",
  name = "Functions should not have too many parameters",
  description = "Checks that functions do not have too many parameters.",
  tags = {Tags.BRAIN_OVERLOAD})
public class FunctionWithTooManyParametersCheck extends LuaCheck {

  private static final int DEFAULT = 7;

  @RuleProperty(
    key = "max",
    description = "Maximum authorized number of parameters.",
    defaultValue = "" + DEFAULT)
  public int max = DEFAULT;

  @Override
  public void init() {
    subscribeTo(LuaGrammar.NAMELIST);
  }

  @Override
  public void visitNode(AstNode astNode) {
    // Only check if this is a parameter list (inside PARLIST)
    AstNode parent = astNode.getParent();
    if (parent != null && parent.is(LuaGrammar.PARLIST)) {
      int nbParameters = astNode.getChildren(LuaGrammar.NAME).size();
      // Also count ellipsis if present in parent
      if (parent.getFirstChild(LuaGrammar.Punctuator.ELLIPSIS) != null) {
        nbParameters++;
      }

      if (nbParameters > max) {
        addIssue(astNode, "This function has {0} parameters, which is greater than the {1} authorized.",
            nbParameters, max);
      }
    }
  }
}
