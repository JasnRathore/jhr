import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Config {
    public String root = "src";
    public String[] watchDirs = {"."};
    public String[] watchExts = {".java"};
    public String[] excludeDirs = {".git", "tmp", "vendor", "target", "build"};
    public String mainClass = "Demo";
    public String[] args = {};
    public String classpath = "src";
    public String javacFlags = "";
    public String jvmArgs = "";
    public int delay = 1000;
    public String buildCmd = "";
    public String logLevel = "info";
    public boolean color = true;
    public boolean clearOnRebuild = true;

    public static Config load(String configFile) {
        Config config = new Config();
        Path path = Paths.get(configFile);
        
        if (!Files.exists(path)) {
            System.out.println("Config file not found: " + configFile);
            System.out.println("Using default configuration.");
            return config;
        }

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                String[] parts = line.split("=", 2);
                if (parts.length != 2) continue;

                String key = parts[0].trim();
                String value = parts[1].trim();

                switch (key) {
                    case "root":
                        config.root = value;
                        break;
                    case "watch_dirs":
                        config.watchDirs = value.isEmpty() ? new String[]{} : value.split(",");
                        for (int i = 0; i < config.watchDirs.length; i++) {
                            config.watchDirs[i] = config.watchDirs[i].trim();
                        }
                        break;
                    case "watch_exts":
                        config.watchExts = value.isEmpty() ? new String[]{} : value.split(",");
                        for (int i = 0; i < config.watchExts.length; i++) {
                            config.watchExts[i] = config.watchExts[i].trim();
                        }
                        break;
                    case "exclude_dirs":
                        config.excludeDirs = value.isEmpty() ? new String[]{} : value.split(",");
                        for (int i = 0; i < config.excludeDirs.length; i++) {
                            config.excludeDirs[i] = config.excludeDirs[i].trim();
                        }
                        break;
                    case "main_class":
                        config.mainClass = value;
                        break;
                    case "args":
                        config.args = value.isEmpty() ? new String[]{} : value.split(" ");
                        break;
                    case "classpath":
                        config.classpath = value.isEmpty() ? config.root : value;
                        break;
                    case "javac_flags":
                        config.javacFlags = value;
                        break;
                    case "jvm_args":
                        config.jvmArgs = value;
                        break;
                    case "delay":
                        config.delay = Integer.parseInt(value);
                        break;
                    case "build_cmd":
                        config.buildCmd = value;
                        break;
                    case "log_level":
                        config.logLevel = value;
                        break;
                    case "color":
                        config.color = Boolean.parseBoolean(value);
                        break;
                    case "clear_on_rebuild":
                        config.clearOnRebuild = Boolean.parseBoolean(value);
                        break;
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading config file: " + e.getMessage());
        }
        
        // If classpath is still default "src" but root is different, use root as classpath
        if (config.classpath.equals("src") && !config.root.equals("src")) {
            config.classpath = config.root;
        }

        return config;
    }

    public static void createDefault(String configFile) throws IOException {
        Path path = Paths.get(configFile);
        if (Files.exists(path)) {
            System.out.println("Config file already exists: " + configFile);
            return;
        }

        String defaultConfig = 
            "# JHR (Java Hot Reload) Configuration File\n" +
            "# Similar to .air.toml for Go\n\n" +
            "# Root directory to watch (where your .java files are)\n" +
            "root = src\n\n" +
            "# Directories to watch (comma-separated, relative to root)\n" +
            "watch_dirs = .\n\n" +
            "# File extensions to watch (comma-separated)\n" +
            "watch_exts = .java\n\n" +
            "# Directories to exclude from watching (comma-separated)\n" +
            "exclude_dirs = .git,tmp,vendor,target,build\n\n" +
            "# Main class to run (e.g., Demo or com.example.Main)\n" +
            "main_class = Main\n\n" +
            "# Additional arguments to pass to the main class\n" +
            "args = \n\n" +
            "# Classpath for compilation and execution (usually same as root)\n" +
            "classpath = demo\n\n" +
            "# Additional compiler flags\n" +
            "javac_flags = \n\n" +
            "# JVM arguments (e.g., -Xmx512m)\n" +
            "jvm_args = \n\n" +
            "# Delay in milliseconds before triggering rebuild after file change\n" +
            "delay = 1000\n\n" +
            "# Build command (leave empty to use default javac)\n" +
            "build_cmd = \n\n" +
            "# Log level: debug, info, warn, error\n" +
            "log_level = info\n\n" +
            "# Color output\n" +
            "color = true\n\n" +
            "# Clear console on rebuild\n" +
            "clear_on_rebuild = true\n";

        Files.writeString(path, defaultConfig);
        System.out.println("Created default config file: " + configFile);
    }
}
