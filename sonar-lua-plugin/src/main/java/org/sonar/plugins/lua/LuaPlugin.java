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

import org.sonar.api.Plugin;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;
import org.sonar.plugins.lua.cobertura.CoberturaSensor;
import org.sonar.plugins.lua.core.Lua;

/**
 * Entry point for the Lua plugin.
 */
public class LuaPlugin implements Plugin {

  public static final String FILE_SUFFIXES_KEY = "sonar.lua.file.suffixes";
  public static final String COBERTURA_REPORT_PATH = "sonar.lua.cobertura.reportPath";

  @Override
  public void define(Context context) {
    context.addExtensions(
      // Language definition
      Lua.class,

      // Sensors
      LuaSensor.class,
      CoberturaSensor.class,

      // Rules and profiles
      LuaRulesDefinition.class,
      LuaProfile.class,

      // Properties
      PropertyDefinition.builder(FILE_SUFFIXES_KEY)
        .defaultValue(Lua.DEFAULT_FILE_SUFFIXES)
        .multiValues(true)
        .name("File suffixes")
        .description("List of suffixes for files to analyze.")
        .onQualifiers(Qualifiers.PROJECT)
        .category("Lua")
        .build(),

      PropertyDefinition.builder(COBERTURA_REPORT_PATH)
        .name("Cobertura XML report path")
        .description("Path to the Cobertura coverage report file. The path may be either absolute or relative to the project base directory.")
        .onQualifiers(Qualifiers.PROJECT)
        .category("Lua")
        .subCategory("Coverage")
        .build()
    );
  }
}
