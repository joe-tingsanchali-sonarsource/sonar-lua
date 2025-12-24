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
import com.sonar.sslr.api.AstNodeType;
import com.sonar.sslr.api.Token;
import com.sonar.sslr.api.Trivia;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Base class for all Lua checks.
 * Provides context for reporting issues and AST traversal.
 */
public abstract class LuaCheck {

  private LuaCheckContext context;
  private final List<AstNodeType> subscribedNodeTypes = new ArrayList<>();

  /**
   * Initialize the check. Subclasses should call subscribeTo() here.
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
   * Set the check context.
   */
  public void setContext(LuaCheckContext context) {
    this.context = context;
  }

  /**
   * Get the check context.
   */
  protected LuaCheckContext getContext() {
    return context;
  }

  /**
   * Get the node types this check is subscribed to.
   */
  public List<AstNodeType> getSubscribedNodeTypes() {
    return subscribedNodeTypes;
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
   * Called for each token (for comment analysis).
   */
  public void visitToken(Token token) {
    // Override in subclasses
  }

  /**
   * Create a file-level issue.
   */
  protected void addFileIssue(String message) {
    context.addIssue(this, message);
  }

  /**
   * Create a file-level issue with message arguments.
   */
  protected void addFileIssue(String message, Object... args) {
    context.addIssue(this, formatMessage(message, args));
  }

  /**
   * Create an issue at a specific line.
   */
  protected void addLineIssue(int line, String message) {
    context.addIssue(this, line, message);
  }

  /**
   * Create an issue at a specific line with message arguments.
   */
  protected void addLineIssue(int line, String message, Object... args) {
    context.addIssue(this, line, formatMessage(message, args));
  }

  /**
   * Create an issue at a specific AST node.
   */
  protected void addIssue(AstNode node, String message) {
    context.addIssue(this, node.getTokenLine(), message);
  }

  /**
   * Create an issue at a specific AST node with message arguments.
   */
  protected void addIssue(AstNode node, String message, Object... args) {
    context.addIssue(this, node.getTokenLine(), formatMessage(message, args));
  }

  /**
   * Create an issue with effort (for remediation cost estimation).
   */
  protected void addIssueWithCost(AstNode node, String message, double cost) {
    context.addIssueWithCost(this, node.getTokenLine(), message, cost);
  }

  /**
   * Get the current file being analyzed.
   */
  protected File getFile() {
    return context.getFile();
  }

  /**
   * Get the charset of the current file.
   */
  protected Charset getCharset() {
    return context.getCharset();
  }

  /**
   * Read lines from the current file.
   */
  protected List<String> readLines() {
    return context.readLines();
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

    // Visit tokens for comment analysis
    Token token = node.getToken();
    if (token != null && !token.getTrivia().isEmpty()) {
      for (Trivia trivia : token.getTrivia()) {
        if (trivia.isComment()) {
          visitToken(trivia.getToken());
        }
      }
    }

    for (AstNode child : node.getChildren()) {
      visit(child);
    }

    if (isSubscribed) {
      leaveNode(node);
    }
  }

  private String formatMessage(String message, Object... args) {
    // Support both {0}, {1} style and %s style formatting
    String formatted = message;
    for (int i = 0; i < args.length; i++) {
      formatted = formatted.replace("{" + i + "}", String.valueOf(args[i]));
      // Also handle {n,number,integer} format
      formatted = formatted.replace("{" + i + ",number,integer}", String.valueOf(args[i]));
    }
    return formatted;
  }
}
