import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

import static java.nio.file.StandardWatchEventKinds.*;

public class Watcher {

    private final WatchService watcher;
    private final Map<WatchKey, Path> keys = new HashMap<>();
    private final Map<Path, Long> lastModified = new HashMap<>();
    private final Map<Path, Long> lastTriggered = new HashMap<>();
    private final OnChangeListener listener;
    private final long debounceDelayMs;

    public interface OnChangeListener {
        void onChange(Path changedPath);
    }

    public Watcher(Path dir, long debounceDelayMs, OnChangeListener listener) throws IOException {
        this.watcher = FileSystems.getDefault().newWatchService();
        this.listener = listener;
        this.debounceDelayMs = debounceDelayMs;
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

                // Ignore class files and backup files
                if (child.toString().endsWith(".class") || 
                    child.toString().contains(".bck") ||
                    child.toString().contains("~")) {
                    continue;
                }

                // For MODIFY events, check if the file was actually modified
                if (kind == ENTRY_MODIFY) {
                    try {
                        if (!Files.exists(child)) {
                            continue;
                        }
                        long currentModTime = Files.getLastModifiedTime(child).toMillis();
                        Long lastMod = lastModified.get(child);
                        
                        // Skip if modification time hasn't changed
                        if (lastMod != null && lastMod == currentModTime) {
                            continue;
                        }
                        lastModified.put(child, currentModTime);
                    } catch (IOException ignored) {
                        // If we can't get mod time, proceed anyway
                    }
                }

                // Debouncing: Skip if we triggered this file recently
                long currentTime = System.currentTimeMillis();
                Long lastTrigger = lastTriggered.get(child);
                if (lastTrigger != null && (currentTime - lastTrigger) < debounceDelayMs) {
                    continue;
                }
                lastTriggered.put(child, currentTime);

                // For CREATE events, store the initial modification time
                if (kind == ENTRY_CREATE) {
                    try {
                        if (Files.isDirectory(child)) {
                            registerAll(child);
                        } else if (Files.exists(child)) {
                            long currentModTime = Files.getLastModifiedTime(child).toMillis();
                            lastModified.put(child, currentModTime);
                        }
                    } catch (IOException ignored) {
                    }
                }

                // For DELETE events, remove from tracking
                if (kind == ENTRY_DELETE) {
                    lastModified.remove(child);
                    lastTriggered.remove(child);
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
