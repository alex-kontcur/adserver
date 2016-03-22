package ru.adserver.infrastructure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.*;
import java.util.Objects;

/**
 * Scanner
 *
 * @author Kontsur Alex (bona)
 * @since 13.11.2015
 */
public class Scanner {

    private static final Logger logger = LoggerFactory.getLogger(Scanner.class);

    public void startScanning(WatchService watchService, String path, ScanAction action) {
        Path directory = Paths.get(path);
        if (!directory.toFile().exists()) {
            throw new IllegalStateException("There is no " + path + " directory");
        }
        logger.info(action.description());
        try {
            WatchKey watchKey = directory.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
            while (true) {
                WatchKey watchKeyActual = watchService.take();
                for (WatchEvent<?> event : watchKeyActual.pollEvents()) {
                    WatchEvent.Kind<?> eventKind = event.kind();
                    if (Objects.equals(eventKind, StandardWatchEventKinds.OVERFLOW)) {
                        continue;
                    }
                    WatchEvent<Path> eventPath = (WatchEvent<Path>) event;
                    String fileName = eventPath.context().getFileName().toString();
                    String targetPath = path + "/" + fileName;
                    action.execute(targetPath, fileName);
                }
                if (!watchKeyActual.reset()) {
                    break;
                }
            }
        } catch (Exception e) {
            logger.error("Stop scanning path " + path, e);
        }
    }

}
