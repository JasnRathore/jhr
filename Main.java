import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.err.println("Usage: java Main <watch_directory> [app args...]");
            System.exit(1);
        }
        String watchDir = args[0];
        String[] runArgs = Arrays.copyOfRange(args, 1, args.length);
        JavaSys sys = new JavaSys();

        Watcher watcher = new Watcher(Paths.get(watchDir), changedPath -> {
            if (changedPath.toString().endsWith(".java")) {
                System.out.println("Change detected in: " + changedPath);
                sys.Compile(changedPath.toString());

                // Example: Assume classpath root is "src" directory containing the package folders
                Path sourceRoot = Paths.get("src"); // adjust accordingly
                Path relativePath = sourceRoot.relativize(changedPath);
                String className = relativePath.toString()
                .replace(".java", "")
                .replace('/', '.')
                .replace("...", "");

                // Run the class - provide classpath root folder (e.g. "src")
                sys.Run(className, "src", runArgs);
            }
        });

        watcher.processEvents();
    }
}
