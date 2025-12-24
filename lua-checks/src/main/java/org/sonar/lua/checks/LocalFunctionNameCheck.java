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
import org.sonar.lua.grammar.LuaGrammar;

import java.util.regex.Pattern;

/**
 * Check that local function names comply with a naming convention.
 */
@Rule(key = "S100")
public class LocalFunctionNameCheck extends LuaCheck {

  private static final String DEFAULT = "^[a-z][a-z_A-Z0-9]*$";
  private Pattern pattern = null;

  @RuleProperty(
    key = "format",
    description = "Regular expression used to check the function names against.",
    defaultValue = DEFAULT)
  public String format = DEFAULT;

  @Override
  public void init() {
    subscribeTo(LuaGrammar.LOCALFUNCSTAT);
  }

  @Override
  public void visitFile(AstNode astNode) {
    if (pattern == null) {
      pattern = Pattern.compile(format);
    }
  }

  @Override
  public void visitNode(AstNode astNode) {
    AstNode nameNode = astNode.getFirstChild(LuaGrammar.NAME);
    if (nameNode != null) {
      String functionName = nameNode.getTokenValue();
      if (!pattern.matcher(functionName).matches()) {
        addIssue(astNode, "Rename this \"{0}\" function to match the regular expression {1}.",
            functionName, format);
      }
    }
  }
}
