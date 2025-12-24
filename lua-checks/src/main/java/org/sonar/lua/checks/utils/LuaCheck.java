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
 * @deprecated Use {@link org.sonar.lua.checks.LuaCheck} instead.
 */
@Deprecated
public abstract class LuaCheck extends org.sonar.lua.checks.LuaCheck {
  // This class is kept for backward compatibility
  // All functionality is in the parent class
}
