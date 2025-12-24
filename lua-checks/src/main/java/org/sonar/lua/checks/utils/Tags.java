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
package org.sonar.lua.checks.utils;

/**
 * Tag constants for rule classification.
 */
public final class Tags {

  public static final String BRAIN_OVERLOAD = "brain-overload";
  public static final String CONVENTION = "convention";
  public static final String PITFALL = "pitfall";
  public static final String BUG = "bug";
  public static final String SECURITY = "security";
  public static final String PERFORMANCE = "performance";

  private Tags() {
    // This class only defines constants
  }
}
