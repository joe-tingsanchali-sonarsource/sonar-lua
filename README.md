# SonarQube Lua Plugin

A SonarQube plugin for analyzing Lua code quality.

## Requirements

- **SonarQube**: SonarQube Community Build (latest) or SonarQube Server 2025.1+
- **Java**: 17 or higher
- **Maven**: 3.8 or higher

> **Note**: This plugin is built for the latest SonarQube APIs. Earlier versions of SonarQube (10.x or older) are not supported.

## Building the Plugin

### Quick Build

```bash
# Clone the repository
git clone https://github.com/SonarQubeCommunity/sonar-lua.git
cd sonar-lua

# Build the plugin
mvn clean package
```

The plugin JAR will be created at:
```
sonar-lua-plugin/target/sonar-lua-plugin-2.0.jar
```

### Running Tests

```bash
mvn clean test
```

Currently, 23 grammar tests verify the Lua parser functionality.

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
│       ├── grammar/     # SSLR grammar definitions (LuaGrammar.java)
│       └── parser/      # Lua parser implementation
├── lua-checks/          # Code quality rules
│   └── src/main/java/org/sonar/lua/checks/
│       ├── LuaCheck.java           # Base class for all checks
│       ├── LuaCheckContext.java    # Context interface for reporting issues
│       ├── CheckList.java          # Registry of all checks
│       ├── utils/Tags.java         # Common rule tags
│       └── *.java                  # Individual check implementations
├── sonar-lua-plugin/    # Plugin assembly
│   └── src/main/java/org/sonar/plugins/lua/
│       ├── LuaPlugin.java          # Plugin entry point
│       ├── LuaSensor.java          # Main analysis sensor
│       ├── LuaRulesDefinition.java # Rule definitions
│       └── LuaProfile.java         # Quality profile
└── pom.xml              # Parent Maven configuration
```

---

## Adding New Rules/Checks

This section explains how to contribute new code quality rules to the plugin.

### Step 1: Create the Check Class

Create a new Java file in `lua-checks/src/main/java/org/sonar/lua/checks/`.

**Example: `GlobalVariableCheck.java`**

```java
package org.sonar.lua.checks;

import com.sonar.sslr.api.AstNode;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.lua.grammar.LuaGrammar;
import org.sonar.lua.checks.utils.Tags;

/**
 * Check that global variables are avoided.
 */
@Rule(
  key = GlobalVariableCheck.CHECK_KEY,
  name = "Global variables should be avoided",
  description = "Global variables can lead to naming conflicts and make code harder to maintain. Use local variables instead.",
  tags = {Tags.PITFALL, Tags.CONVENTION}
)
public class GlobalVariableCheck extends LuaCheck {

  public static final String CHECK_KEY = "GlobalVariable";

  // Optional: configurable property
  @RuleProperty(
    key = "allowedGlobals",
    description = "Comma-separated list of allowed global variable names",
    defaultValue = "_G,_VERSION")
  public String allowedGlobals = "_G,_VERSION";

  @Override
  public void init() {
    // Subscribe to AST node types you want to analyze
    subscribeTo(LuaGrammar.STATEMENT);
  }

  @Override
  public void visitNode(AstNode node) {
    // Analyze the node and report issues
    if (isGlobalAssignment(node)) {
      addIssue(node, "Avoid using global variables. Use 'local' instead.");
    }
  }

  private boolean isGlobalAssignment(AstNode node) {
    // Your detection logic here
    return false;
  }
}
```

### Step 2: Understanding the `@Rule` Annotation

The `@Rule` annotation is **required** and must include:

| Attribute | Required | Description |
|-----------|----------|-------------|
| `key` | ✅ Yes | Unique identifier for the rule (e.g., `"S104"`, `"GlobalVariable"`) |
| `name` | ✅ Yes | Human-readable rule name displayed in SonarQube |
| `description` | ✅ Yes | Explanation of what the rule checks |
| `tags` | Optional | Categories like `Tags.BRAIN_OVERLOAD`, `Tags.CONVENTION`, `Tags.PITFALL` |

**Available Tags** (defined in `org.sonar.lua.checks.utils.Tags`):

```java
Tags.BRAIN_OVERLOAD  // Complexity-related issues
Tags.CONVENTION      // Coding convention violations
Tags.PITFALL         // Potential bugs or gotchas
Tags.BUG             // Likely bugs
Tags.UNUSED          // Unused code
```

### Step 3: Using the `LuaCheck` Base Class

All checks must extend `LuaCheck`, which provides these methods:

| Method | Description |
|--------|-------------|
| `subscribeTo(LuaGrammar.XXX)` | Register to visit specific AST node types |
| `addIssue(AstNode, String)` | Report an issue at a specific AST node |
| `addIssue(AstNode, String, Object...)` | Report with message formatting |
| `addFileIssue(String)` | Report an issue at file level |
| `addFileIssue(String, Object...)` | File-level issue with formatting |
| `addLineIssue(int, String)` | Report at a specific line number |
| `getFile()` | Get the current file being analyzed |
| `readLines()` | Get all lines in the current file |

### Step 4: Subscribing to AST Node Types

Use `LuaGrammar` constants in `init()` to specify which nodes to visit:

```java
@Override
public void init() {
  // Single node type
  subscribeTo(LuaGrammar.FUNCTION);
  
  // Multiple node types
  subscribeTo(
    LuaGrammar.IF_STATEMENT,
    LuaGrammar.WHILE_STATEMENT,
    LuaGrammar.FOR_STATEMENT
  );
}
```

**Common `LuaGrammar` node types:**

| Node Type | Description |
|-----------|-------------|
| `CHUNK` | Root node of the AST |
| `STATEMENT` | Any statement |
| `FUNCTION` | Function definition |
| `LOCAL_FUNCTION` | Local function definition |
| `FUNCSTAT` | Named function statement |
| `IF_STATEMENT` | If/elseif/else block |
| `WHILE_STATEMENT` | While loop |
| `FOR_STATEMENT` | For loop |
| `REPEAT_STATEMENT` | Repeat-until loop |
| `TABLECONSTRUCTOR` | Table literal `{}` |
| `FIELDLIST` | Fields inside a table |
| `FIELD` | Single table field |
| `FUNCTIONCALL` | Function invocation |
| `NAME` | Identifier |
| `EXP` | Expression |

### Step 5: Register the Rule

Add your check class to `lua-checks/src/main/java/org/sonar/lua/checks/CheckList.java`:

```java
public static List<Class<?>> getChecks() {
  return Arrays.asList(
    // ... existing checks ...
    GlobalVariableCheck.class  // Add your new check here
  );
}
```

### Step 6: Add Rule Documentation (Optional but Recommended)

Create HTML documentation in `lua-checks/src/main/resources/org/sonar/l10n/lua/rules/lua/`:

**`GlobalVariable.html`:**
```html
<p>
  Global variables can cause naming conflicts across different Lua files
  and make code harder to maintain and test.
</p>

<h2>Noncompliant Code Example</h2>
<pre>
myGlobal = "value"  -- Noncompliant: global variable
</pre>

<h2>Compliant Solution</h2>
<pre>
local myLocal = "value"  -- Compliant: local variable
</pre>
```

### Step 7: Build and Test

```bash
# Rebuild the plugin
mvn clean package

# The JAR is at:
# sonar-lua-plugin/target/sonar-lua-plugin-2.0.jar
```

### Creating Template Rules

Template rules allow users to create custom instances with different parameters (like XPath queries or regex patterns). Mark them with `setTemplate(true)` in `LuaRulesDefinition.java`:

```java
// In LuaRulesDefinition.java
if ("XPath".equals(rule.key()) || "MyTemplateRule".equals(rule.key())) {
  rule.setTemplate(true);
}
```

### Example: Complete Check Implementation

Here's a complete example of a rule that checks for functions with too many local variables:

```java
package org.sonar.lua.checks;

import com.sonar.sslr.api.AstNode;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.lua.grammar.LuaGrammar;
import org.sonar.lua.checks.utils.Tags;

@Rule(
  key = TooManyLocalsCheck.CHECK_KEY,
  name = "Functions should not have too many local variables",
  description = "Having too many local variables in a function makes it harder to understand.",
  tags = {Tags.BRAIN_OVERLOAD}
)
public class TooManyLocalsCheck extends LuaCheck {

  public static final String CHECK_KEY = "TooManyLocals";
  private static final int DEFAULT_MAX = 15;

  @RuleProperty(
    key = "max",
    description = "Maximum number of local variables allowed",
    defaultValue = "" + DEFAULT_MAX)
  public int max = DEFAULT_MAX;

  @Override
  public void init() {
    subscribeTo(LuaGrammar.FUNCTION, LuaGrammar.LOCAL_FUNCTION);
  }

  @Override
  public void visitNode(AstNode node) {
    int localCount = countLocalVariables(node);
    if (localCount > max) {
      addIssue(node, 
        "This function has {0} local variables, which is greater than {1} allowed.",
        localCount, max);
    }
  }

  private int countLocalVariables(AstNode functionNode) {
    // Count LOCAL_STATEMENT nodes within this function
    return functionNode.getDescendants(LuaGrammar.LOCAL_STATEMENT).size();
  }
}
```

---

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
| `CommentRegularExpression` | Comment Pattern | Comments matching pattern should be flagged (template) |
| `XPath` | XPath Rule | Custom node-matching rule (template) |

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

## Debugging

To debug the plugin during analysis:

1. Start SonarScanner in debug mode:
   ```bash
   export SONAR_SCANNER_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005"
   sonar-scanner
   ```

2. Connect your IDE debugger to port 5005.

## License

This project is licensed under the GNU Lesser General Public License v3.0 (LGPL-3.0).

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Run `mvn clean package` to ensure it builds
5. Run `mvn test` to verify tests pass
6. Submit a pull request

### Contribution Ideas

- Add new code quality rules
- Improve Lua grammar coverage
- Add support for Lua 5.4 features
- Improve code coverage integration
- Add support for LuaRocks dependency analysis

## Links

- [SonarQube Documentation](https://docs.sonarsource.com/sonarqube/latest/)
- [SonarQube Plugin Development](https://docs.sonarsource.com/sonarqube-server/extension-guide/)
- [Lua Reference Manual](https://www.lua.org/manual/5.4/)
- [SSLR (SonarSource Language Recognizer)](https://github.com/SonarSource/sslr)
