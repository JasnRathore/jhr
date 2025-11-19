import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

import static java.nio.file.StandardWatchEventKinds.*;

public class Watcher {

    private final WatchService watcher;
    private final Map<WatchKey, Path> keys = new HashMap<>();
    private final OnChangeListener listener;

    public interface OnChangeListener {
        void onChange(Path changedPath);
    }

    public Watcher(Path dir, OnChangeListener listener) throws IOException {
        this.watcher = FileSystems.getDefault().newWatchService();
        this.listener = listener;
        registerAll(dir);
    }

    private void register(Path dir) throws IOException {
        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        keys.put(key, dir);
    }

    private void registerAll(final Path start) throws IOException {
        Files.walkFileTree(start, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                register(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public void processEvents() {
        while (true) {
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            Path dir = keys.get(key);
            if (dir == null) {
                continue;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();
                Path name = (Path) event.context();
                Path child = dir.resolve(name);
                System.out.println("Change detected in: " + child);

                //To Ignore Class Files
                if (child.toString().endsWith(".class")) {
                    continue;
                }

                if (kind == ENTRY_CREATE) {
                    try {
                        if (Files.isDirectory(child)) {
                            registerAll(child);
                        }
                    } catch (IOException ignored) {
                    }
                }

                listener.onChange(child);
            }

            boolean valid = key.reset();
            if (!valid) {
                keys.remove(key);
                if (keys.isEmpty()) break;
            }
        }
    }
}

