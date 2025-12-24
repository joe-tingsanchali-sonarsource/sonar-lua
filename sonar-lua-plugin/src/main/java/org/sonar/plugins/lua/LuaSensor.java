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
package org.sonar.plugins.lua;

import com.sonar.sslr.api.AstNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.rule.RuleKey;
import org.sonar.check.Rule;
import org.sonar.lua.checks.CheckList;
import org.sonar.lua.checks.LuaCheck;
import org.sonar.lua.checks.LuaCheckContext;
import org.sonar.lua.grammar.LuaGrammar;
import org.sonar.lua.parser.LuaParser;
import org.sonar.plugins.lua.core.Lua;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Main sensor for Lua code analysis.
 */
public class LuaSensor implements Sensor {

  private static final Logger LOG = LoggerFactory.getLogger(LuaSensor.class);

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor
      .name("Lua Sensor")
      .onlyOnLanguage(Lua.KEY)
      .onlyOnFileType(InputFile.Type.MAIN);
  }

  @Override
  public void execute(SensorContext context) {
    FileSystem fileSystem = context.fileSystem();
    FilePredicates predicates = fileSystem.predicates();
    Charset charset = fileSystem.encoding();

    LuaParser parser = new LuaParser(charset);
    List<LuaCheck> checks = instantiateChecks();

    for (InputFile inputFile : fileSystem.inputFiles(
        predicates.and(
          predicates.hasType(InputFile.Type.MAIN),
          predicates.hasLanguage(Lua.KEY)
        ))) {
      analyzeFile(context, inputFile, parser, checks, charset);
    }
  }

  private void analyzeFile(SensorContext context, InputFile inputFile, LuaParser parser, 
                           List<LuaCheck> checks, Charset charset) {
    try {
      File file = inputFile.file();
      AstNode rootNode = parser.parse(file);

      // Create context for checks
      LuaFileCheckContext checkContext = new LuaFileCheckContext(file, charset, rootNode, inputFile);

      // Run all checks
      for (LuaCheck check : checks) {
        check.setContext(checkContext);
        check.init();
        check.scanTree(rootNode);
      }

      // Save issues
      saveIssues(context, inputFile, checkContext);

      // Save metrics
      saveMetrics(context, inputFile, rootNode, checkContext);

    } catch (Exception e) {
      LOG.error("Failed to analyze file: " + inputFile.filename(), e);
    }
  }

  private List<LuaCheck> instantiateChecks() {
    List<LuaCheck> checks = new ArrayList<>();
    for (Class<?> checkClass : CheckList.getChecks()) {
      try {
        Object instance = checkClass.getDeclaredConstructor().newInstance();
        if (instance instanceof LuaCheck) {
          checks.add((LuaCheck) instance);
        }
      } catch (Exception e) {
        LOG.error("Failed to instantiate check: " + checkClass.getName(), e);
      }
    }
    return checks;
  }

  private void saveIssues(SensorContext context, InputFile inputFile, LuaFileCheckContext checkContext) {
    for (LuaFileCheckContext.IssueInfo issue : checkContext.getIssues()) {
      RuleKey ruleKey = RuleKey.of(CheckList.REPOSITORY_KEY, issue.getRuleKey());
      NewIssue newIssue = context.newIssue().forRule(ruleKey);
      
      NewIssueLocation location = newIssue.newLocation()
        .on(inputFile)
        .message(issue.getMessage());

      if (issue.getLine() > 0) {
        location.at(inputFile.selectLine(issue.getLine()));
      }

      newIssue.at(location);

      if (issue.getCost() > 0) {
        newIssue.gap(issue.getCost());
      }

      newIssue.save();
    }
  }

  private void saveMetrics(SensorContext context, InputFile inputFile, AstNode rootNode, 
                           LuaFileCheckContext checkContext) {
    List<String> lines = checkContext.readLines();

    // Lines of code (non-blank lines)
    int linesOfCode = countLinesOfCode(lines);
    context.<Integer>newMeasure()
      .on(inputFile)
      .forMetric(CoreMetrics.NCLOC)
      .withValue(linesOfCode)
      .save();

    // Comment lines
    int commentLines = countCommentLines(rootNode);
    context.<Integer>newMeasure()
      .on(inputFile)
      .forMetric(CoreMetrics.COMMENT_LINES)
      .withValue(commentLines)
      .save();

    // Functions
    int functions = countNodes(rootNode, LuaGrammar.FUNCSTAT, LuaGrammar.LOCALFUNCSTAT, LuaGrammar.FUNCTION);
    context.<Integer>newMeasure()
      .on(inputFile)
      .forMetric(CoreMetrics.FUNCTIONS)
      .withValue(functions)
      .save();

    // Statements
    int statements = countNodes(rootNode, LuaGrammar.STATEMENT);
    context.<Integer>newMeasure()
      .on(inputFile)
      .forMetric(CoreMetrics.STATEMENTS)
      .withValue(statements)
      .save();

    // Complexity
    int complexity = calculateComplexity(rootNode);
    context.<Integer>newMeasure()
      .on(inputFile)
      .forMetric(CoreMetrics.COMPLEXITY)
      .withValue(complexity)
      .save();

    // Classes (tables in Lua)
    int tables = countNodes(rootNode, LuaGrammar.TABLECONSTRUCTOR);
    context.<Integer>newMeasure()
      .on(inputFile)
      .forMetric(CoreMetrics.CLASSES)
      .withValue(tables)
      .save();
  }

  private int countLinesOfCode(List<String> lines) {
    int count = 0;
    for (String line : lines) {
      if (!line.trim().isEmpty()) {
        count++;
      }
    }
    return count;
  }

  private int countCommentLines(AstNode rootNode) {
    // Simple estimation based on comment trivia
    return countCommentsRecursively(rootNode);
  }

  private int countCommentsRecursively(AstNode node) {
    int count = 0;
    if (node.getToken() != null) {
      count += node.getToken().getTrivia().stream()
        .filter(t -> t.isComment())
        .mapToInt(t -> t.getToken().getValue().split("\n").length)
        .sum();
    }
    for (AstNode child : node.getChildren()) {
      count += countCommentsRecursively(child);
    }
    return count;
  }

  private int countNodes(AstNode rootNode, LuaGrammar... types) {
    int count = 0;
    for (LuaGrammar type : types) {
      count += countNodesOfType(rootNode, type);
    }
    return count;
  }

  private int countNodesOfType(AstNode node, LuaGrammar type) {
    int count = 0;
    if (node.is(type)) {
      count++;
    }
    for (AstNode child : node.getChildren()) {
      count += countNodesOfType(child, type);
    }
    return count;
  }

  private int calculateComplexity(AstNode rootNode) {
    int complexity = 1; // Base complexity
    complexity += countNodes(rootNode, LuaGrammar.IF_STATEMENT);
    complexity += countNodes(rootNode, LuaGrammar.WHILE_STATEMENT);
    complexity += countNodes(rootNode, LuaGrammar.FOR_STATEMENT);
    complexity += countNodes(rootNode, LuaGrammar.REPEAT_STATEMENT);
    // Count elseif keywords (add complexity)
    complexity += countElseIfKeywords(rootNode);
    // Count boolean operators
    complexity += countBooleanOperators(rootNode);
    return complexity;
  }

  private int countElseIfKeywords(AstNode node) {
    int count = 0;
    if (node.is(LuaGrammar.Keyword.ELSEIF)) {
      count++;
    }
    for (AstNode child : node.getChildren()) {
      count += countElseIfKeywords(child);
    }
    return count;
  }

  private int countBooleanOperators(AstNode node) {
    int count = 0;
    if (node.is(LuaGrammar.BINOP)) {
      String value = node.getTokenValue();
      if ("and".equals(value) || "or".equals(value)) {
        count++;
      }
    }
    for (AstNode child : node.getChildren()) {
      count += countBooleanOperators(child);
    }
    return count;
  }

  /**
   * Implementation of LuaCheckContext for file analysis.
   */
  private static class LuaFileCheckContext implements LuaCheckContext {
    private final File file;
    private final Charset charset;
    private final AstNode rootNode;
    private final InputFile inputFile;
    private final List<IssueInfo> issues = new ArrayList<>();
    private List<String> lines;

    LuaFileCheckContext(File file, Charset charset, AstNode rootNode, InputFile inputFile) {
      this.file = file;
      this.charset = charset;
      this.rootNode = rootNode;
      this.inputFile = inputFile;
    }

    @Override
    public File getFile() {
      return file;
    }

    @Override
    public Charset getCharset() {
      return charset;
    }

    @Override
    public List<String> readLines() {
      if (lines == null) {
        try {
          lines = Files.readAllLines(file.toPath(), charset);
        } catch (IOException e) {
          lines = new ArrayList<>();
        }
      }
      return lines;
    }

    @Override
    public AstNode getRootNode() {
      return rootNode;
    }

    @Override
    public void addIssue(LuaCheck check, String message) {
      addIssue(check, 0, message);
    }

    @Override
    public void addIssue(LuaCheck check, int line, String message) {
      String ruleKey = getRuleKey(check);
      if (ruleKey != null) {
        issues.add(new IssueInfo(ruleKey, line, message, 0));
      }
    }

    @Override
    public void addIssueWithCost(LuaCheck check, int line, String message, double cost) {
      String ruleKey = getRuleKey(check);
      if (ruleKey != null) {
        issues.add(new IssueInfo(ruleKey, line, message, cost));
      }
    }

    private String getRuleKey(LuaCheck check) {
      Rule annotation = check.getClass().getAnnotation(Rule.class);
      return annotation != null ? annotation.key() : null;
    }

    List<IssueInfo> getIssues() {
      return issues;
    }

    static class IssueInfo {
      private final String ruleKey;
      private final int line;
      private final String message;
      private final double cost;

      IssueInfo(String ruleKey, int line, String message, double cost) {
        this.ruleKey = ruleKey;
        this.line = line;
        this.message = message;
        this.cost = cost;
      }

      String getRuleKey() {
        return ruleKey;
      }

      int getLine() {
        return line;
      }

      String getMessage() {
        return message;
      }

      double getCost() {
        return cost;
      }
    }
  }
}

