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

/**
 * Template check that can be configured via an XPath-like node type selector.
 * This is a simplified version that matches by node type name.
 */
@Rule(key = "XPath")
public class XPathCheck extends LuaCheck {

  private static final String DEFAULT_XPATH_QUERY = "";
  private static final String DEFAULT_MESSAGE = "The XPath expression matches this piece of code.";

  @RuleProperty(
    key = "xpathQuery",
    description = "The node type name to match (e.g., FUNCTION, IF_STATEMENT).",
    defaultValue = DEFAULT_XPATH_QUERY)
  public String xpathQuery = DEFAULT_XPATH_QUERY;

  @RuleProperty(
    key = "message",
    description = "The issue message.",
    defaultValue = DEFAULT_MESSAGE)
  public String message = DEFAULT_MESSAGE;

  @Override
  public void visitFile(AstNode astNode) {
    if (xpathQuery != null && !xpathQuery.isEmpty()) {
      findMatchingNodes(astNode);
    }
  }

  private void findMatchingNodes(AstNode node) {
    // Match by node type name
    if (node.getType() != null && node.getType().toString().equalsIgnoreCase(xpathQuery)) {
      addIssue(node, message);
    }
    
    // Recursively check children
    for (AstNode child : node.getChildren()) {
      findMatchingNodes(child);
    }
  }

  public String getXPathQuery() {
    return xpathQuery;
  }

  public String getMessage() {
    return message;
  }
}
