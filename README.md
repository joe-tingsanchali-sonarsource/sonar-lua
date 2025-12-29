# SonarQube Lua Plugin

A SonarQube plugin for analyzing Lua code quality.

## Requirements

- **SonarQube**: 26.1 or higher (for SonarQube Server 2025.1 or higher)
- **Java**: 17 or higher
- **Maven**: 3.8 or higher

## Building the Plugin

### Quick Build

```bash
# Clone the repository
git clone https://github.com/SonarQubeCommunity/sonar-lua.git
cd sonar-lua

# Build the plugin (skip tests)
mvn clean package -Dmaven.test.skip=true
```

The plugin JAR will be created at:
```
sonar-lua-plugin/target/sonar-lua-plugin-2.0.jar
```

### Full Build with Tests

```bash
mvn clean package
```

> **Note**: Some tests may need to be updated to work with the latest dependencies.

## Installation

1. Copy `sonar-lua-plugin-2.0.jar` to SonarQube's extensions directory:
   ```bash
   cp sonar-lua-plugin/target/sonar-lua-plugin-2.0.jar $SONARQUBE_HOME/extensions/plugins/
   ```

2. Restart SonarQube:
   ```bash
   $SONARQUBE_HOME/bin/<your-os>/sonar.sh restart
   ```

3. Verify the plugin is installed by navigating to:
   **Administration → Marketplace → Installed**

## Analyzing a Lua Project

### 1. Create `sonar-project.properties`

Create a file named `sonar-project.properties` in your Lua project root:

```properties
# Required metadata
sonar.projectKey=my-lua-project
sonar.projectName=My Lua Project
sonar.projectVersion=1.0

# Source directories
sonar.sources=src
# Or for root-level files:
# sonar.sources=.

# File encoding
sonar.sourceEncoding=UTF-8

# Lua-specific settings
sonar.lua.file.suffixes=lua

# Optional: Cobertura coverage report
# sonar.lua.cobertura.reportPath=coverage.xml
```

### 2. Run Analysis

Using SonarScanner:
```bash
sonar-scanner
```

Or with Maven (if you have a `pom.xml`):
```bash
mvn sonar:sonar
```

### 3. View Results

After analysis completes, visit your SonarQube instance to view the results.

## Development

### Project Structure

```
sonar-lua/
├── lua-squid/           # Lua parser and AST utilities
│   └── src/main/java/org/sonar/lua/
│       ├── grammar/     # SSLR grammar definitions
│       └── parser/      # Lua parser implementation
├── lua-checks/          # Code quality rules
│   └── src/main/java/org/sonar/lua/checks/
│       ├── LuaCheck.java           # Base class for all checks
│       ├── LuaCheckContext.java    # Context interface
│       ├── CheckList.java          # Registry of all checks
│       └── *.java                  # Individual check implementations
├── sonar-lua-plugin/    # Plugin assembly
│   └── src/main/java/org/sonar/plugins/lua/
│       ├── LuaPlugin.java          # Plugin entry point
│       ├── LuaSensor.java          # Main analysis sensor
│       ├── LuaRulesDefinition.java # Rule definitions
│       └── LuaProfile.java         # Quality profile
└── pom.xml              # Parent Maven configuration
```

### Creating a New Rule

1. **Create the check class** in `lua-checks/src/main/java/org/sonar/lua/checks/`:

```java
package org.sonar.lua.checks;

import com.sonar.sslr.api.AstNode;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.lua.grammar.LuaGrammar;

@Rule(key = "MyNewRule")
public class MyNewRuleCheck extends LuaCheck {

  @RuleProperty(
    key = "threshold",
    description = "The threshold value.",
    defaultValue = "10")
  public int threshold = 10;

  @Override
  public void init() {
    subscribeTo(LuaGrammar.FUNCTION);  // Subscribe to AST node types
  }

  @Override
  public void visitNode(AstNode node) {
    // Analyze the node
    if (/* violation condition */) {
      addIssue(node, "Issue message here.");
    }
  }
}
```

2. **Add rule description** in `lua-checks/src/main/resources/org/sonar/l10n/lua/rules/lua/`:
   - Create `MyNewRule.html` with the rule description
   - Create `MyNewRule.json` with rule metadata (optional)

3. **Register the rule** in `CheckList.java`:

```java
public static List<Class> getChecks() {
  return Arrays.asList(
    // ... existing checks ...
    MyNewRuleCheck.class
  );
}
```

4. **Rebuild** the plugin:
```bash
mvn clean package -Dmaven.test.skip=true
```

### Debugging

To debug the plugin in SonarQube:

1. Start SonarQube in debug mode:
   ```bash
   export SONAR_SCANNER_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
   ```

2. Connect your IDE debugger to port 5005.

### Running in IDE

Import the project as a Maven project. The main classes to explore:

- `LuaGrammar.java` - Defines the Lua language grammar
- `LuaSensor.java` - Main entry point for analysis
- `LuaCheck.java` - Base class for all checks

## Metrics

The plugin computes the following metrics:

| Metric | Description |
|--------|-------------|
| `LINES_OF_CODE` | Non-blank lines of code |
| `LINES` | Total lines |
| `FILES` | Number of files |
| `COMMENT_LINES` | Lines containing comments |
| `FUNCTIONS` | Number of functions |
| `STATEMENTS` | Number of statements |
| `CLASSES` | Number of tables (Lua's object equivalent) |
| `COMPLEXITY` | Cyclomatic complexity |

### Complexity Calculation

The following elements increment complexity by one:

- Function definitions (`function`, `local function`)
- Control flow statements (`if`, `while`, `for`, `repeat`)
- Boolean operators (`and`, `or`)
- `elseif` clauses
- `break` statements

## Rules

| Rule Key | Name | Description |
|----------|------|-------------|
| `FunctionComplexity` | Function Complexity | Functions should not be too complex |
| `MethodComplexity` | Method Complexity | Methods should not be too complex |
| `LocalFunctionComplexity` | Local Function Complexity | Local functions should not be too complex |
| `FuncCaLL` | Function Call Complexity | Function calls should not be too complex |
| `FileComplexity` | File Complexity | Files should not be too complex |
| `TableComplexity` | Table Complexity | Tables should not be too complex |
| `S107` | Too Many Parameters | Functions should not have too many parameters |
| `TableParameter` | Too Many Fields | Tables should not have too many fields |
| `S134` | Nested Control Flow | Control flow should not be nested too deeply |
| `NestedFunction` | Nested Functions | Functions should not be nested too deeply |
| `NestedTable` | Nested Tables | Tables should not be nested too deeply |
| `S100` | Function Naming | Local function names should match convention |
| `LineLength` | Line Length | Lines should not be too long |
| `S104` | File Length | Files should not have too many lines |
| `CommentRegularExpression` | Comment Pattern | Comments matching pattern should be flagged |
| `XPath` | XPath Rule | Custom XPath-based rule template |

## Configuration Properties

| Property | Description | Default |
|----------|-------------|---------|
| `sonar.lua.file.suffixes` | File extensions to analyze | `lua` |
| `sonar.lua.cobertura.reportPath` | Path to Cobertura coverage report | (none) |

## Coverage

The plugin supports importing code coverage data from Cobertura XML reports:

```properties
sonar.lua.cobertura.reportPath=coverage.xml
```

## License

This project is licensed under the GNU Lesser General Public License v3.0 (LGPL-3.0).

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Run `mvn clean package` to ensure it builds
5. Submit a pull request

## Links

- [SonarQube Documentation](https://docs.sonarsource.com/sonarqube/latest/)
- [SonarQube Plugin Development](https://docs.sonarsource.com/sonarqube-server/extension-guide/)
- [Lua Reference Manual](https://www.lua.org/manual/5.4/)
