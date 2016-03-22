package ru.adserver.service;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.adserver.domain.model.AdEntry;
import ru.adserver.domain.repository.AdRepository;
import ru.adserver.infrastructure.IndexTask;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * AdsManager
 *
 * @author Kontsur Alex (bona)
 * @since 14.11.2015
 */
@Service
public class AdsManager {

    private static final Logger logger = LoggerFactory.getLogger(AdsManager.class);

    @Inject
    private AdsPersister adsPersister;

    @Inject
    private AdRepository repository;

    @Inject
    private IndexWriter indexWriter;


    @Value("${ad.live-period.days}")
    private int adLivePeriod;

    @SuppressWarnings("CollectionDeclaredAsConcreteClass")
    private ConcurrentHashMap<String, AdInfo> adInfoMap;

    @PostConstruct
    protected void postConstruct() {
        adInfoMap = new ConcurrentHashMap<>();
        refreshInfoFromDB();
    }

    private void refreshInfoFromDB() {
        int pageSize = 100;
        long count = repository.count();
        int totalPages = (int) Math.ceil((double) count / pageSize);
        for (int pageNumber = 0; pageNumber < totalPages; pageNumber++) {
            Page<AdEntry> entries = repository.findAll(new PageRequest(pageNumber, pageSize));
            for (AdEntry adEntry : entries) {
                adInfoMap.put(adEntry.getNum(), new AdInfo(adEntry.getHits(), adEntry.getLastAccess().getMillis()));
            }
        }
    }

    @Scheduled(fixedDelay = 5000)
    public void evictExpiredAds() {
        try {
            for (Map.Entry<String, AdInfo> entry : adInfoMap.entrySet()) {
                String num = entry.getKey();
                AdInfo adInfo = entry.getValue();
                long livePeriodMs = TimeUnit.MILLISECONDS.convert(adLivePeriod, TimeUnit.DAYS);
                if (adInfo.getCreated() + livePeriodMs <= System.currentTimeMillis()) {
                    indexWriter.deleteDocuments(new Term("num", num));
                    adsPersister.scheduleDrop(num);
                    adInfoMap.remove(num);
                }
            }
        } catch (Exception e) {
            logger.error("Error while evicting expired ads ->", e);
        }
    }

    public void indexAds(IndexTask indexTask) {
        long start = System.currentTimeMillis();
        for (Map.Entry<String, AdInfo> entry : adInfoMap.entrySet()) {
            AdInfo adInfo = entry.getValue();
            if (adInfo.getIndexed()) {
                continue;
            }
            String num = entry.getKey();
            adInfo.setIndexed(indexTask.execute(num + ".html"));
        }
    }

    public void addAdInfo(String num) {
        adInfoMap.put(num, new AdInfo());
    }

    public void updateAdInfo(String num) {
        AdInfo adInfo = adInfoMap.get(num);
        if (adInfo == null) {
            return;
        }
        adInfo.getHits().incrementAndGet();
        adInfo.setLassAccessTime(DateTime.now().getMillis());
        adsPersister.scheduleSave(num, adInfo);
    }

    public boolean adExists(String num) {
        return adInfoMap.containsKey(num);
    }

}
