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

import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinitionAnnotationLoader;
import org.sonar.lua.checks.CheckList;

/**
 * Defines the rules available for Lua analysis.
 */
public final class LuaRulesDefinition implements RulesDefinition {

  private static final String REPOSITORY_NAME = "SonarQube Lua";

  @Override
  public void define(Context context) {
    NewRepository repository = context
      .createRepository(CheckList.REPOSITORY_KEY, "lua")
      .setName(REPOSITORY_NAME);

    // Load rules from annotations
    RulesDefinitionAnnotationLoader annotationLoader = new RulesDefinitionAnnotationLoader();
    annotationLoader.load(repository, CheckList.getChecks().toArray(new Class[0]));

    repository.done();
  }
}
