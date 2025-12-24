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
import com.sonar.sslr.api.Token;
import com.sonar.sslr.api.Trivia;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;

import java.util.regex.Pattern;

/**
 * Check that comments don't match a configurable regular expression.
 * This is a rule template for creating custom comment rules.
 */
@Rule(key = "CommentRegularExpression")
public class CommentRegularExpressionCheck extends LuaCheck {

  private static final String DEFAULT_REGULAR_EXPRESSION = "";
  private static final String DEFAULT_MESSAGE = "The regular expression matches this comment.";

  @RuleProperty(
    key = "regularExpression",
    description = "The regular expression to match against comments.",
    defaultValue = DEFAULT_REGULAR_EXPRESSION)
  public String regularExpression = DEFAULT_REGULAR_EXPRESSION;

  @RuleProperty(
    key = "message",
    description = "The issue message.",
    defaultValue = DEFAULT_MESSAGE)
  public String message = DEFAULT_MESSAGE;

  private Pattern pattern;

  @Override
  public void visitFile(AstNode astNode) {
    if (regularExpression != null && !regularExpression.isEmpty()) {
      pattern = Pattern.compile(regularExpression);
      visitNodeRecursively(astNode);
    }
  }

  private void visitNodeRecursively(AstNode node) {
    Token token = node.getToken();
    if (token != null) {
      for (Trivia trivia : token.getTrivia()) {
        if (trivia.isComment()) {
          String comment = trivia.getToken().getValue();
          if (pattern.matcher(comment).find()) {
            addLineIssue(trivia.getToken().getLine(), message);
          }
        }
      }
    }

    for (AstNode child : node.getChildren()) {
      visitNodeRecursively(child);
    }
  }

  public String getRegularExpression() {
    return regularExpression;
  }

  public String getMessage() {
    return message;
  }
}
