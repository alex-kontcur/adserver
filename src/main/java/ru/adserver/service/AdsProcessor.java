package ru.adserver.service;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.adserver.infrastructure.FsReader;
import ru.adserver.infrastructure.IndexTask;
import ru.adserver.infrastructure.ScanAction;
import ru.adserver.infrastructure.Scanner;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.nio.file.WatchService;
import java.util.Date;

/**
 * AdsProcessor
 *
 * @author Kontsur Alex (bona)
 * @since 13.11.2015
 */
@Service
@DependsOn({"segmentNotFoundHacker", "adsManager"})
public class AdsProcessor implements PropertyObserver {

    private static final Logger logger = LoggerFactory.getLogger(AdsProcessor.class);

    private static final String ADS_PATH_PROPERTY = "ads.path";

    @Value("${ads.path}")
    private volatile String adsPath;

    @Value("${index.path}")
    private String indexPath;

    @Inject
    private TaskScheduler scheduler;

    @Inject
    private IndexWriter indexWriter;

    @Inject
    private AdsManager adsManager;

    @Inject
    private FsReader fsReader;

    private WatchService watchService;

    @PostConstruct
    public void init() {
        long millis = new DateTime().plusSeconds(3).getMillis();
        scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                processExistingFiles();
                scanAds();
            }
        }, new Date(millis));
    }

    private void processExistingFiles() {
        File adsFolder = new File(adsPath);
        if (!adsFolder.exists()) {
            throw new IllegalStateException("There is no " + adsPath + " directory");
        }
        if (adsFolder.list().length == 0) {
            return;
        }
        try {
            IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath).toFile()));
            IndexSearcher indexSearcher = new IndexSearcher(reader);
            File[] files = adsFolder.listFiles();
            for (File file : files) {
                indexFile(indexSearcher, file.getName());
            }
        } catch (Exception e) {
            logger.error("Error while processExistingFiles ->", e);
        }
    }

    @Scheduled(fixedDelay = 5000, initialDelay = 5000)
    public void indexAds() {
        try {
            IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath).toFile()));
            final IndexSearcher indexSearcher = new IndexSearcher(reader);
            adsManager.indexAds(new IndexTask() {
                @Override
                public boolean execute(String fileName) {
                    return indexFile(indexSearcher, fileName);
                }
            });
        } catch (Exception e) {
            logger.error("Error while index Ads ->", e);
        }
    }

    public boolean fileIndexed(IndexSearcher indexSearcher, String num) {
        BooleanQuery matchingQuery = new BooleanQuery();
        matchingQuery.add(new TermQuery(new Term("num", num)), BooleanClause.Occur.SHOULD);
        try {
            TopDocs results = indexSearcher.search(matchingQuery, 1);
            return results.totalHits > 0;
        } catch (Exception ignored) {
        }
        return false;
    }

    private boolean indexFile(IndexSearcher indexSearcher, String fileName) {
        try {
            String num = fileName.split(".html")[0].trim();
            if (fileIndexed(indexSearcher, num)) {
                return true;
            }
            String content = fsReader.read(adsPath + "/" + fileName, false);
            if (content == null) {
                return false;
            }
            Document doc = new Document();
            doc.add(new StringField("num", num, Field.Store.YES));
            doc.add(new TextField("content", content, Field.Store.NO));
            indexWriter.addDocument(doc);
            indexWriter.commit();

            return true;
        } catch (Exception e) {
            logger.error("Error while indexing file \"" + fileName + "\" ->", e);
        }
        return false;
    }

    @SuppressWarnings("OverlyNestedMethod")
    private void scanAds() {
        try {
            watchService = FileSystems.getDefault().newWatchService();
            Scanner scanner = new Scanner();
            scanner.startScanning(watchService, adsPath, new ScanAction() {
                @Override
                public void execute(String targetPath, String fileName) {
                    String num = fileName.split(".html")[0].trim();
                    adsManager.addAdInfo(num);
                }
                @Override
                public String description() {
                    return String.format("Start scanning \"%s\" for initing ads", adsPath);
                }
            });
        } catch (Exception e) {
            logger.error("Error while scanning folder \"" + adsPath + "\" -> ", e);
        }
    }

    @Override
    public void propertyChanged(String name, String value) {
        if (name.equals(ADS_PATH_PROPERTY)) {
            adsPath = value;
            try {
                watchService.close();
                scanAds();
            } catch (Exception e) {
                logger.error("Error while initing watchService ->", e);
            }
        }
    }

    @PreDestroy
    public void destory() {
        try {
            indexWriter.close();
        } catch (IOException ignored) {
        }
    }
}
