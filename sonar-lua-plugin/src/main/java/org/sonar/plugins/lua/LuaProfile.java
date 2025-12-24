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

import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;
import org.sonar.check.Rule;
import org.sonar.lua.checks.CheckList;
import org.sonar.plugins.lua.core.Lua;

/**
 * Defines the default quality profile for Lua.
 */
public class LuaProfile implements BuiltInQualityProfilesDefinition {

  @Override
  public void define(Context context) {
    NewBuiltInQualityProfile profile = context.createBuiltInQualityProfile(
        CheckList.SONAR_WAY_PROFILE, Lua.KEY);
    profile.setDefault(true);

    // Activate all rules from the CheckList
    for (Class<?> checkClass : CheckList.getChecks()) {
      Rule ruleAnnotation = checkClass.getAnnotation(Rule.class);
      if (ruleAnnotation != null) {
        String ruleKey = ruleAnnotation.key();
        if (ruleKey != null && !ruleKey.isEmpty()) {
          profile.activateRule(CheckList.REPOSITORY_KEY, ruleKey);
        }
      }
    }

    profile.done();
  }
}
