package ru.adserver.infrastructure;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import ru.adserver.service.PropertyObserver;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.WatchService;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RuntimeConfig
 *
 * @author Kontsur Alex (bona)
 * @since 13.11.2015
 */
@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
@Service
public class RuntimeConfig {

    private static final Logger logger = LoggerFactory.getLogger(RuntimeConfig.class);

    @Value("${runtime.properies.path}")
    private String runtimePath;

    @Inject
    private TaskScheduler scheduler;

    @Inject
    private FsReader fsReader;

    @Inject
    private List<PropertyObserver> observers;

    private Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private Map<String, String> properties;


    @PostConstruct
    public void init() {
        long millis = new DateTime().plusSeconds(3).getMillis();
        scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                scanForProperiesUpdate();
            }
        }, new Date(millis));
    }

    @SuppressWarnings("OverlyNestedMethod")
    private void scanForProperiesUpdate() {
        try {
            WatchService fileSystemWatchService = FileSystems.getDefault().newWatchService();
            Scanner scanner = new Scanner();
            scanner.startScanning(fileSystemWatchService, runtimePath, new ScanAction() {
                @Override
                public void execute(String targetPath, String fileName) {
                    String value = properties.get(fileName);
                    if (value == null) {
                        logger.warn("property \"{}\" doesn't exists", fileName);
                    } else {
                        String content = fsReader.read(targetPath, false);
                        if (content != null) {
                            properties.put(fileName, content);
                            for (PropertyObserver observer : observers) {
                                observer.propertyChanged(fileName, content);
                            }
                        }
                    }
                    FileUtils.deleteQuietly(new File(targetPath));
                }
                @Override
                public String description() {
                    return String.format("Start scanning \"%s\" for new runtime settings", runtimePath);
                }
            });
        } catch (Exception e) {
            logger.error("Error while scanning folder \"" + runtimePath + "\" -> ", e);
        }
    }

    public String getProperty(String propertyName) {
        return properties.get(propertyName);
    }

    public String getRuntimeProperties() {
        return gson.toJson(properties);
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = new ConcurrentHashMap<>(properties);
    }

}
