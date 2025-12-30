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
 * Check that tables don't have too many fields.
 */
@Rule(
  key = "TableParameter",
  name = "Tables should not have too many fields",
  description = "Checks that tables do not have too many fields.",
  tags = {Tags.BRAIN_OVERLOAD})
public class TableWithTooManyFieldsCheck extends LuaCheck {

  private static final int DEFAULT = 5;

  @RuleProperty(
    key = "max",
    description = "Maximum authorized number of fields.",
    defaultValue = "" + DEFAULT)
  public int max = DEFAULT;

  @Override
  public void init() {
    subscribeTo(LuaGrammar.FIELDLIST);
  }

  @Override
  public void visitNode(AstNode astNode) {
    int nbFields = astNode.getChildren(LuaGrammar.FIELD).size();
    if (nbFields > max) {
      addIssue(astNode, "This table has {0} fields, which is greater than the {1} authorized.",
          nbFields, max);
    }
  }
}
