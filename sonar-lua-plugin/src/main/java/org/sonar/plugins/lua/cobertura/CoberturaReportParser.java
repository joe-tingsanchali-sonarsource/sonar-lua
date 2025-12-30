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
package org.sonar.plugins.lua.cobertura;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.coverage.NewCoverage;
import org.sonar.plugins.lua.core.Lua;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Parser for Cobertura XML coverage reports.
 */
public class CoberturaReportParser {

  private static final Logger LOG = LoggerFactory.getLogger(CoberturaReportParser.class);

  private CoberturaReportParser() {
  }

  /**
   * Parse a Cobertura xml report and create measures accordingly.
   */
  public static void parseReport(File xmlFile, final SensorContext context) {
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      // Disable external entities for security
      factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
      factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
      
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document document = builder.parse(xmlFile);
      
      NodeList packages = document.getElementsByTagName("package");
      collectPackageMeasures(packages, context);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to parse Cobertura report: " + xmlFile.getAbsolutePath(), e);
    }
  }

  private static void collectPackageMeasures(NodeList packages, SensorContext context) {
    for (int i = 0; i < packages.getLength(); i++) {
      Element pack = (Element) packages.item(i);
      NodeList classes = pack.getElementsByTagName("class");
      collectFileMeasures(context, classes);
    }
  }

  private static void collectFileMeasures(SensorContext context, NodeList classes) {
    FileSystem fileSystem = context.fileSystem();
    FilePredicates predicates = fileSystem.predicates();
    Map<String, InputFile> inputFileByFilename = new HashMap<>();
    
    for (int i = 0; i < classes.getLength(); i++) {
      Element clazz = (Element) classes.item(i);
      String fileName = clazz.getAttribute("filename");

      InputFile inputFile;
      if (inputFileByFilename.containsKey(fileName)) {
        inputFile = inputFileByFilename.get(fileName);
      } else {
        String key = fileName.startsWith(File.separator) ? fileName : (File.separator + fileName);
        inputFile = fileSystem.inputFile(predicates.and(
          predicates.matchesPathPattern("file:**" + key.replace(File.separator, "/")),
          predicates.hasType(InputFile.Type.MAIN),
          predicates.hasLanguage(Lua.KEY)));
        inputFileByFilename.put(fileName, inputFile);
        if (inputFile == null && !fileName.endsWith(".mxml")) {
          LOG.warn("Cannot save coverage result for file: {}, because resource not found.", fileName);
        }
      }
      if (inputFile != null) {
        collectFileData(
          clazz,
          context.newCoverage()
            .onFile(inputFile)
        );
      }
    }
  }

  private static void collectFileData(Element clazz, NewCoverage newCoverage) {
    NodeList linesElements = clazz.getElementsByTagName("lines");
    if (linesElements.getLength() > 0) {
      Element linesElement = (Element) linesElements.item(0);
      NodeList lines = linesElement.getElementsByTagName("line");
      
      for (int i = 0; i < lines.getLength(); i++) {
        Element line = (Element) lines.item(i);
        int lineId = Integer.parseInt(line.getAttribute("number"));
        int hits = Integer.parseInt(line.getAttribute("hits"));
        newCoverage.lineHits(lineId, hits);

        String isBranch = line.getAttribute("branch");
        String text = line.getAttribute("condition-coverage");
        if ("true".equals(isBranch) && text != null && !text.isEmpty()) {
          String[] conditions = extractConditions(text);
          if (conditions.length == 2) {
        newCoverage.conditions(lineId, Integer.parseInt(conditions[1]), Integer.parseInt(conditions[0]));
          }
        }
      }
    }
    newCoverage.save();
  }

  private static String[] extractConditions(String text) {
    // Extract "X/Y" from "XX% (X/Y)"
    int start = text.indexOf('(');
    int end = text.indexOf(')');
    if (start >= 0 && end > start) {
      String conditionPart = text.substring(start + 1, end);
      return conditionPart.split("/");
    }
    return new String[0];
  }
}
