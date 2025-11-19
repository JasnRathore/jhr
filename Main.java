import java.nio.file.*;
import java.util.stream.Stream;

public class Main {
    private static final String VERSION = "0.1.0";
    private static final String CONFIG_FILE = ".jhr.conf";

    public static void main(String[] args) throws Exception {
        // Parse command line arguments
        if (args.length > 0) {
            switch (args[0]) {
                case "init":
                    Config.createDefault(CONFIG_FILE);
                    return;
                case "version":
                case "-v":
                case "--version":
                    System.out.println("JHR (Java Hot Reload) v" + VERSION);
                    return;
                case "help":
                case "-h":
                case "--help":
                    printHelp();
                    return;
            }
        }

        // Load configuration
        Config config = Config.load(CONFIG_FILE);
        
        printBanner(config);

        JavaSys sys = new JavaSys(config);
        Path sourceRoot = Paths.get(config.root);

        // Initial compilation
        if (config.clearOnRebuild) {
            clearScreen();
        }
        
        System.out.println("🔨 Building...");
        System.out.println("─────────────────────────────────────");
        
        boolean compilationSuccess = true;
        try (Stream<Path> paths = Files.walk(sourceRoot)) {
            for (Path javaFile : paths.filter(p -> {
                String pathStr = p.toString();
                // Check if it's a Java file
                if (!pathStr.endsWith(".java")) return false;
                
                // Check if it's in an excluded directory
                for (String excluded : config.excludeDirs) {
                    if (pathStr.contains(excluded)) return false;
                }
                return true;
            }).toArray(Path[]::new)) {
                if (!sys.Compile(javaFile.toString())) {
                    compilationSuccess = false;
                    break;
                }
            }
        }

        // Only run if compilation succeeded
        if (compilationSuccess) {
            System.out.println("\n🚀 Starting application: " + config.mainClass);
            System.out.println("─────────────────────────────────────");
            
            // If root directory is different from classpath, use root as classpath
            String actualClasspath = config.classpath;
            if (!config.root.equals(".") && !config.root.equals(config.classpath)) {
                actualClasspath = config.root;
            }
            
            sys.Run(config.mainClass, actualClasspath, config.args);
        } else {
            System.err.println("\n⚠  Skipping application start due to compilation errors");
        }

        System.out.println("\n👀 Watching for changes in: " + config.root);
        System.out.println("   Press Ctrl+C to stop\n");

        // Start watching for changes
        Watcher watcher = new Watcher(sourceRoot, config.delay, changedPath -> {
            // Check if file should be watched
            String pathStr = changedPath.toString();
            boolean shouldWatch = false;
            
            for (String ext : config.watchExts) {
                if (pathStr.endsWith(ext)) {
                    shouldWatch = true;
                    break;
                }
            }
            
            if (!shouldWatch) return;

            // Check if in excluded directory
            for (String excluded : config.excludeDirs) {
                if (pathStr.contains(excluded)) return;
            }

            if (config.clearOnRebuild) {
                clearScreen();
            }

            System.out.println("📝 Change detected: " + changedPath);
            System.out.println("─────────────────────────────────────");
            System.out.println("🔨 Rebuilding...\n");
            
            boolean success = sys.Compile(changedPath.toString());

            if (success) {
                Path relativePath = sourceRoot.relativize(changedPath);
                String className = relativePath.toString()
                    .replace(".java", "")
                    .replace('/', '.')
                    .replace('\\', '.');

                System.out.println("\n🚀 Restarting application...");
                System.out.println("─────────────────────────────────────");
                sys.Run(config.mainClass, config.classpath, config.args);
            } else {
                System.err.println("\n⚠  Skipping restart due to compilation errors");
                System.err.println("   Fix the errors and save to retry\n");
            }
        });

        watcher.processEvents();
    }

    private static void printBanner(Config config) {
        System.out.println("  ___ _  _ ___  ");
        System.out.println(" |_  | || | _ \\ ");
        System.out.println("  | | __ |   / ");
        System.out.println(" |___|_||_|_|_\\ v" + VERSION);
        System.out.println();
        System.out.println(" Java Hot Reload");
        System.out.println(" Like Air, but for Java");
        System.out.println();
    }

    private static void clearScreen() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception ignored) {
            // If clear fails, just continue
        }
    }

    private static void printHelp() {
        System.out.println("JHR (Java Hot Reload) - Like Air, but for Java\n");
        System.out.println("Usage:");
        System.out.println("  jhr              Start with .jhr.conf in current directory");
        System.out.println("  jhr init         Create a default .jhr.conf file");
        System.out.println("  jhr version      Show version information");
        System.out.println("  jhr help         Show this help message");
        System.out.println();
        System.out.println("For more information, visit: https://github.com/yourusername/jhr");
    }
}
