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
package org.sonar.lua.checks.utils;

import com.sonar.sslr.api.AstNode;
import org.sonar.lua.grammar.LuaGrammar;

/**
 * Utility class for function-related operations.
 */
public final class Function {

  private Function() {
    // Utility class
  }

  /**
   * Get the name of a local function.
   */
  public static String getName(AstNode functionDef) {
    if (!functionDef.is(LuaGrammar.LOCALFUNCSTAT)) {
      throw new IllegalArgumentException("Expected LOCALFUNCSTAT node");
    }
    AstNode nameNode = functionDef.getFirstChild(LuaGrammar.NAME);
    return nameNode != null ? nameNode.getTokenValue() : "";
  }

  /**
   * Get the name of a local function.
   */
  public static String getLocalName(AstNode functionDef) {
    return getName(functionDef);
  }

  /**
   * Check if a function is an empty constructor.
   */
  public static boolean isEmptyConstructor(AstNode functionDef, String className) {
    if (!functionDef.is(LuaGrammar.LOCALFUNCSTAT)) {
      throw new IllegalArgumentException("Expected LOCALFUNCSTAT node");
    }
    AstNode funcBody = functionDef.getFirstChild(LuaGrammar.FUNCBODY);
    if (funcBody == null) {
      return false;
    }
    AstNode block = funcBody.getFirstChild(LuaGrammar.BLOCK);
    
    return isConstructor(functionDef, className)
        && (block == null || block.getFirstChild(LuaGrammar.CHUNK) == null 
            || block.getFirstChild(LuaGrammar.CHUNK).getChildren().isEmpty());
  }

  /**
   * Check if a function is a constructor.
   */
  public static boolean isConstructor(AstNode functionDef, String className) {
    if (!functionDef.is(LuaGrammar.LOCALFUNCSTAT)) {
      throw new IllegalArgumentException("Expected LOCALFUNCSTAT node");
    }
    AstNode nameNode = functionDef.getFirstChild(LuaGrammar.NAME);
    return nameNode != null 
        && nameNode.getNumberOfChildren() == 1
        && nameNode.getFirstChild().getTokenValue().equals(className);
  }
}
