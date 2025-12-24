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
 * Calculates cyclomatic complexity for Lua code.
 * Complexity is incremented by:
 * - 1 for each function/method
 * - 1 for each if/elseif
 * - 1 for each while/for/repeat loop
 * - 1 for each 'and' / 'or' boolean operator
 */
public final class ComplexityCalculator {

  private ComplexityCalculator() {
    // Utility class
  }

  /**
   * Calculate the complexity of an AST subtree.
   */
  public static int calculate(AstNode node) {
    int complexity = 0;
    
    // Base complexity for decision points
    if (isComplexityNode(node)) {
      complexity++;
    }
    
    // Check for boolean operators
    if (node.is(LuaGrammar.BINOP)) {
      String value = node.getTokenValue();
      if ("and".equals(value) || "or".equals(value)) {
        complexity++;
      }
    }
    
    // Recurse into children
    for (AstNode child : node.getChildren()) {
      complexity += calculate(child);
    }
    
    return complexity;
  }

  /**
   * Calculate complexity of a file (root node).
   */
  public static int calculateFileComplexity(AstNode rootNode) {
    // Start with 1 for the file itself
    return 1 + calculate(rootNode);
  }

  /**
   * Calculate complexity of a function body.
   */
  public static int calculateFunctionComplexity(AstNode funcNode) {
    AstNode funcBody = funcNode.getFirstChild(LuaGrammar.FUNCBODY);
    if (funcBody != null) {
      AstNode block = funcBody.getFirstChild(LuaGrammar.BLOCK);
      if (block != null) {
        return 1 + calculate(block);
      }
    }
    return 1;
  }

  private static boolean isComplexityNode(AstNode node) {
    return node.is(
      LuaGrammar.IF_STATEMENT,
      LuaGrammar.WHILE_STATEMENT,
      LuaGrammar.FOR_STATEMENT,
      LuaGrammar.REPEAT_STATEMENT
    ) || isElseIf(node);
  }

  private static boolean isElseIf(AstNode node) {
    // elseif adds complexity
    if (node.is(LuaGrammar.Keyword.ELSEIF)) {
      return true;
    }
    return false;
  }
}

