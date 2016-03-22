package ru.adserver.service;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.adserver.domain.model.AdEntry;
import ru.adserver.domain.repository.AdRepository;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * AdsPersister
 *
 * @author Kontsur Alex (bona)
 * @since 14.11.2015
 */
@Component
public class AdsPersister {

    @Value("${ads-persister.batch-size}")
    private int batchSize;

    @Inject
    private AdRepository repository;

    private BlockingQueue<AdEntry> saveQueue;
    private BlockingQueue<String> dropQueue;

    @PostConstruct
    protected void postConstruct() {
        saveQueue = new LinkedBlockingQueue<>();
        dropQueue = new LinkedBlockingQueue<>();
    }

    @PreDestroy
    protected void preDestroy() {
        if (saveQueue.isEmpty()) {
            return;
        }
        while (!saveQueue.isEmpty()) {
            saveBatch();
        }
        while (!dropQueue.isEmpty()) {
            dropBatch();
        }
    }

    @Scheduled(fixedDelayString = "${ads-persister.drop-period.ms}")
    public void drop() {
        while (dropQueue.size() >= batchSize) {
            dropBatch();
        }
    }

    @Scheduled(fixedDelayString = "${ads-persister.flush-period.ms}")
    public void flush() {
        while (saveQueue.size() >= batchSize) {
            saveBatch();
        }
    }

    public void scheduleDrop(String num) {
        dropQueue.add(num);
    }

    public void scheduleSave(String num, AdInfo adInfo) {
        try {
            AdEntry adEntry = repository.findByNum(num);
            if (adEntry == null) {
                adEntry = new AdEntry(num);
            }
            adEntry.setHits(adInfo.getHits().get());
            adEntry.setLastAccess(new DateTime(adInfo.getLassAccessTime()));

            saveQueue.put(adEntry);
        } catch (InterruptedException ignored) {
        }
    }

    private void dropBatch() {
        Collection<String> batch = new ArrayList<>();
        try {
            dropQueue.drainTo(batch, batchSize);
            if (batch.isEmpty()) {
                return;
            }
            repository.deleteByNums(batch);
            repository.flush();
        } catch (Exception ignored) {
            for (String num : batch) {
                repository.deleteByNum(num);
            }
        }
    }

    private void saveBatch() {
        Collection<AdEntry> batch = new ArrayList<>();
        try {
            saveQueue.drainTo(batch, batchSize);
            if (batch.isEmpty()) {
                return;
            }
            repository.save(batch);
            repository.flush();
        } catch (Exception ignored) {
            for (AdEntry adEntry : batch) {
                singleStore(adEntry);
            }
        }
    }

    private void singleStore(AdEntry adEntry) {
        try {
            repository.saveAndFlush(adEntry);
        } catch (Exception ignored) {
        }
    }

}
