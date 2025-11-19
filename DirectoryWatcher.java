import static java.nio.file.StandardWatchEventKinds.*;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

public class DirectoryWatcher {

    private final WatchService watcher;
    private final Map<WatchKey, Path> keys;

    public DirectoryWatcher(Path dir) throws IOException {
        this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<>();
        registerAll(dir);
    }

    // Register the given directory with the WatchService
    private void register(Path dir) throws IOException {
        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        keys.put(key, dir);
    }

    // Register the directory and all its subdirectories
    private void registerAll(final Path start) throws IOException {
        Files.walkFileTree(start, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                register(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    // Process all events for keys queued to the watcher
    public void processEvents() {
        while (true) {
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException e) {
                return;
            }

            Path dir = keys.get(key);
            if (dir == null) {
                System.err.println("WatchKey not recognized!");
                continue;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();

                // Context for directory entry event is the file name of entry
                Path name = (Path) event.context();
                Path child = dir.resolve(name);

                System.out.format("%s: %s%n", kind.name(), child);

                // If a directory is created, register it and its sub-directories
                if (kind == ENTRY_CREATE) {
                    try {
                        if (Files.isDirectory(child)) {
                            registerAll(child);
                        }
                    } catch (IOException e) {
                        // Ignore to keep sample readable
                    }
                }

                // Here, if a java source file is modified or created, you can trigger recompilation and reload class
                if ((kind == ENTRY_CREATE || kind == ENTRY_MODIFY) && child.toString().endsWith(".java")) {
                    System.out.println("Trigger recompilation and reload for " + child);
                    // Add your dynamic compile and reload logic here
                }
            }

            boolean valid = key.reset();
            if (!valid) {
                keys.remove(key);
                if (keys.isEmpty()) {
                    break;
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        Path dir = Paths.get("src"); // Specify directory to watch
        DirectoryWatcher watcher = new DirectoryWatcher(dir);
        watcher.processEvents();
    }
}

