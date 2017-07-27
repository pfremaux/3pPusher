package verticle.rest.config;

import java.io.IOException;
import java.nio.file.*;
import java.util.function.Consumer;

public final class ConfigWatcher implements Runnable {

    private final WatchService watcher;
    private Consumer<Path> creation;
    private Consumer<Path> update;
    private Consumer<Path> delete;
    private Path pathToWatch;

    public ConfigWatcher(String path, Consumer<Path> creation, Consumer<Path> update, Consumer<Path> delete) throws IOException {
        pathToWatch = Paths.get(path);
        watcher = FileSystems.getDefault().newWatchService();
        this.creation = creation;
        this.update = update;
        this.delete = delete;
    }

    @Override
    public void run() {
        watch();
    }

    public void watch() {

        try {
            WatchKey key = pathToWatch.register(watcher,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE,
                    StandardWatchEventKinds.ENTRY_MODIFY);

        } catch (IOException x) {
            System.err.println(x);
        }

        for (; ; ) {

            // wait for key to be signaled
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                return;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();

                // This key is registered only
                // for ENTRY_CREATE events,
                // but an OVERFLOW event can
                // occur regardless if events
                // are lost or discarded.
                if (kind == StandardWatchEventKinds.OVERFLOW) {
                    continue;
                } else if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                    Path pathChanged = getValidatedPath((WatchEvent<Path>) event);
                    creation.accept(pathChanged);
                    continue;
                } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                    Path pathChanged = getValidatedPath((WatchEvent<Path>) event);
                    update.accept(pathChanged);
                    continue;
                }
                if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                    Path pathChanged = getValidatedPath((WatchEvent<Path>) event);
                    delete.accept(pathChanged);
                    continue;
                }

                // The filename is the
                // context of the event.

                WatchEvent<Path> ev = (WatchEvent<Path>) event;
                Path filename = ev.context();
                // Verify that the new
                //  file is a text file.


                // Email the file to the
                //  specified email alias.
                System.out.format("Emailing file %s%n", filename);
                //Details left to reader....
            }

            // Reset the key -- this step is critical if you want to
            // receive further watch events.  If the key is no longer valid,
            // the directory is inaccessible so exit the loop.
            boolean valid = key.reset();
            if (!valid) {
                break;
            }
        }
    }

    private Path getValidatedPath(WatchEvent<Path> event) {
        WatchEvent<Path> ev = event;
        Path path = ev.context();
        /*try {
            // Resolve the filename against the directory.
            // If the filename is "test" and the directory is "foo",
            // the resolved name is "test/foo".
            Path child = pathToWatch.resolve(path);
            if (!"text/plain".equals(Files.probeContentType(child))) {
                System.err.format("New file '%s'" +
                        " is not a plain text file.%n", child.toString());
            }
        } catch (IOException x) {
            System.err.println(x);
        }*/
        return path;
    }

}
