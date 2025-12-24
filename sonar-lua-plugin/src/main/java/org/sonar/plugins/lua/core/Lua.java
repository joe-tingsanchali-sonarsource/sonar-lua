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
package org.sonar.plugins.lua.core;

import org.sonar.api.config.Configuration;
import org.sonar.api.resources.AbstractLanguage;
import org.sonar.plugins.lua.LuaPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Definition of the Lua language for SonarQube.
 */
public class Lua extends AbstractLanguage {

  public static final String NAME = "Lua";
  public static final String KEY = "lua";
  public static final String DEFAULT_FILE_SUFFIXES = "lua";

  private final Configuration config;

  /**
   * Creates the Lua language definition.
   * This constructor is called by the IoC container.
   */
  public Lua(Configuration config) {
    super(KEY, NAME);
    this.config = config;
  }

  @Override
  public String[] getFileSuffixes() {
    String[] suffixes = config.getStringArray(LuaPlugin.FILE_SUFFIXES_KEY);
    if (suffixes == null || suffixes.length == 0) {
      suffixes = DEFAULT_FILE_SUFFIXES.split(",");
    }
    return filterEmptyStrings(suffixes);
  }

  private static String[] filterEmptyStrings(String[] stringArray) {
    List<String> nonEmptyStrings = new ArrayList<>();
    for (String string : stringArray) {
      String trimmed = string.trim();
      if (!trimmed.isEmpty()) {
        nonEmptyStrings.add(trimmed);
      }
    }
    return nonEmptyStrings.toArray(new String[0]);
  }
}
