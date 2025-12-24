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
package org.sonar.lua;

import com.sonar.sslr.api.AstNode;
import com.sonar.sslr.api.AstNodeType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Base visitor for Lua AST traversal.
 * Subclasses can subscribe to specific node types and implement visiting logic.
 */
public abstract class LuaVisitor {

  private final List<AstNodeType> subscribedNodeTypes = new ArrayList<>();

  /**
   * Initialize the visitor. Subclasses should call subscribeTo() here.
   */
  public void init() {
    // Override in subclasses
  }

  /**
   * Subscribe to specific AST node types.
   */
  protected void subscribeTo(AstNodeType... nodeTypes) {
    subscribedNodeTypes.addAll(Arrays.asList(nodeTypes));
  }

  /**
   * Called when starting to visit a file.
   */
  public void visitFile(AstNode astNode) {
    // Override in subclasses
  }

  /**
   * Called when finishing visiting a file.
   */
  public void leaveFile(AstNode astNode) {
    // Override in subclasses
  }

  /**
   * Called when entering a subscribed node.
   */
  public void visitNode(AstNode astNode) {
    // Override in subclasses
  }

  /**
   * Called when leaving a subscribed node.
   */
  public void leaveNode(AstNode astNode) {
    // Override in subclasses
  }

  /**
   * Get the node types this visitor is subscribed to.
   */
  public List<AstNodeType> getSubscribedNodeTypes() {
    return subscribedNodeTypes;
  }

  /**
   * Scan the AST tree and invoke visitor methods.
   */
  public void scanTree(AstNode rootNode) {
    visitFile(rootNode);
    visit(rootNode);
    leaveFile(rootNode);
  }

  private void visit(AstNode node) {
    boolean isSubscribed = subscribedNodeTypes.isEmpty() || 
                           subscribedNodeTypes.contains(node.getType());
    
    if (isSubscribed) {
      visitNode(node);
    }

    for (AstNode child : node.getChildren()) {
      visit(child);
    }

    if (isSubscribed) {
      leaveNode(node);
    }
  }
}

