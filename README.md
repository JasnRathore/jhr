Here's your README, crafted for a developer audience:

````markdown
# JHR — Java Hot Reload

> Like Air, but for Java.

If you've ever worked with Go's [Air](https://github.com/air-verse/air) tool, you know how freeing it is to save a file and watch your app instantly restart. **JHR brings that same workflow to Java.** No more manual `javac` + `java` cycles. No more forgetting to recompile. Just save, and JHR handles the rest.

---

## What It Does

JHR watches your source directory for changes to `.java` files, recompiles on the fly, and restarts your application automatically. It even surfaces a desktop overlay window when compilation fails — so you can't miss an error even if your terminal is buried under other windows.

---

## Requirements

- **Java 11+** (uses `WatchService`, `Files.walk`, and modern switch expressions)
- A Unix-like shell or Windows (clear-screen behavior adapts per OS)
- No external build tools required — JHR uses `javac` and `java` directly

---

## Project Structure

```
jhr/
├── Main.java          # Entry point; orchestrates startup, watching, and rebuilding
├── Config.java        # Loads .jhr.conf configuration file
├── JavaSys.java       # Handles compilation (javac) and process management (java)
├── Watcher.java       # File system watcher with debouncing logic
├── ErrorOverlay.java  # Swing-based desktop overlay for build/runtime errors
├── build              # Shell script to compile JHR itself and package jhr.jar
├── jhr                # Shell launcher script
└── demo/              # Example Java project to test JHR against
    ├── Main.java
    ├── Test.java
    └── test2/
        └── IAmTest.java
```

---

## Getting Started

### 1. Build JHR

Run the included build script to compile all source files and package them into `jhr.jar`:

```bash
chmod +x build
./build
```

This produces `target/jhr.jar` and copies the `jhr` launcher script into `target/`.

### 2. Make the Launcher Executable

```bash
chmod +x target/jhr
```

Optionally, add `target/` to your `PATH` so you can call `jhr` from anywhere:

```bash
export PATH="$PATH:/path/to/jhr/target"
```

### 3. Initialize a Config File

Inside your Java project directory, run:

```bash
jhr init
```

This creates a `.jhr.conf` file with sensible defaults. Open it up and point it at your project:

```ini
# Root directory containing your .java source files
root = src

# The fully-qualified main class to run
main_class = com.example.Main

# Classpath for compilation and execution
classpath = src

# Milliseconds to wait after a change before rebuilding
delay = 1000
```

---

## Configuration Reference

JHR is configured entirely via `.jhr.conf` — a simple key=value file. Here's a full breakdown:

| Key               | Default                          | Description                                      |
|-------------------|----------------------------------|--------------------------------------------------|
| `root`            | `src`                            | Source directory to watch                        |
| `watch_dirs`      | `.`                              | Subdirectories to watch (comma-separated)        |
| `watch_exts`      | `.java`                          | File extensions that trigger a rebuild           |
| `exclude_dirs`    | `.git,tmp,vendor,target,build`   | Directories to ignore                            |
| `main_class`      | `Demo`                           | Main class to launch after compilation           |
| `args`            | _(empty)_                        | Arguments passed to your main class              |
| `classpath`       | `src`                            | Classpath for `javac` and `java`                 |
| `javac_flags`     | _(empty)_                        | Extra flags forwarded to `javac`                 |
| `jvm_args`        | _(empty)_                        | JVM flags like `-Xmx512m`                        |
| `delay`           | `1000`                           | Debounce delay in milliseconds                   |
| `build_cmd`       | _(empty)_                        | Optional custom build command (bypasses javac)   |
| `log_level`       | `info`                           | Verbosity: `debug`, `info`, `warn`, `error`      |
| `color`           | `true`                           | Enable colored terminal output                   |
| `clear_on_rebuild`| `true`                           | Clear the terminal before each rebuild           |

---

## Running JHR

### Basic Usage

From your project root (where `.jhr.conf` lives):

```bash
jhr
```

JHR will:
1. Compile all `.java` files under `root`
2. Launch your `main_class`
3. Watch for file changes and restart on every save

### CLI Commands

```bash
jhr              # Start hot-reload using .jhr.conf
jhr init         # Generate a default .jhr.conf
jhr version      # Print version info
jhr help         # Show usage
```

---

## Example: Running the Demo Project

The repo ships with a small Swing demo under `demo/`. Try it out:

```bash
cd /path/to/jhr

# Create a config pointing at the demo
cat > .jhr.conf <<EOF
root = demo
main_class = Main
classpath = demo
delay = 800
EOF

jhr
```

A small Swing window will appear. Open `demo/Test.java`, change the string returned by `getContent()`, and save. JHR detects the change, recompiles, and relaunches — all within a second.

---

## Error Overlay

When a compilation fails, JHR doesn't just print to stderr and hope you notice. It opens a dedicated **Swing overlay window** pinned above other windows, showing:

- The file that failed to compile
- Full `javac` error output in a scrollable pane
- A reminder that JHR will auto-reload once you fix the issue

Once you save a corrected file, the overlay dismisses itself automatically.

---

## How It Works Internally

JHR is built around three core ideas:

**Watching** — `Watcher.java` uses Java's `WatchService` API to register recursive listeners on your source tree. It debounces rapid sequential saves to avoid redundant rebuilds.

**Compiling** — `JavaSys.compile()` forks a `javac` process with the configured classpath and flags. Compiled classes land in a `temp/` directory separate from your source.

**Running** — `JavaSys.run()` forks a `java` process with both your original classpath and `temp/` on the classpath. On each rebuild, the old process is forcibly killed before the new one starts.

---

## Tips

- Set `delay` higher (e.g., `2000`) if your IDE auto-saves frequently and causes unnecessary rebuilds.
- Use `jvm_args = -ea` to enable Java assertions during development.
- If you have a multi-module project, point `build_cmd` at your build script and JHR will delegate compilation entirely to it.

---

## Contributing

JHR is intentionally minimal — a single-process Java tool with no external dependencies. If you'd like to contribute, the best places to start are:

- **Incremental compilation**: Currently JHR recompiles only the changed file. Dependency tracking across files would be a meaningful improvement.
- **Windows testing**: The file watcher and process management paths have light Windows support but need more coverage.
- **Maven/Gradle integration**: A `build_cmd` hook is there — good examples and docs would help.

Pull requests and issues are welcome.

---

*JHR — because the feedback loop between writing Java and seeing it run should be measured in milliseconds, not keystrokes.*
````
