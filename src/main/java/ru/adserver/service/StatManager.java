package ru.adserver.service;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.adserver.domain.model.AdEntry;
import ru.adserver.domain.repository.AdRepository;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * StatManager
 *
 * @author Kontsur Alex (bona)
 * @since 14.11.2015
 */
@Service
public class StatManager {

    @Inject
    private AdRepository repository;

    @Value("${statistics.count}")
    private int statisticsCount;

    public List<AdEntry> getTopAds24() {
        return getAds(TimeUnit.MILLISECONDS.convert(24, TimeUnit.HOURS), true);
    }

    public List<AdEntry> getTopAds4() {
        return getAds(TimeUnit.MILLISECONDS.convert(4, TimeUnit.HOURS), true);
    }

    public List<AdEntry> getTopAds1() {
        return getAds(TimeUnit.MILLISECONDS.convert(1, TimeUnit.HOURS), true);
    }

    public List<AdEntry> getTopAdsMinute() {
        return getAds(TimeUnit.MILLISECONDS.convert(1, TimeUnit.MINUTES), true);
    }

    public List<AdEntry> getBottomAds24() {
        return getAds(TimeUnit.MILLISECONDS.convert(24, TimeUnit.HOURS), false);
    }

    public List<AdEntry> getBottomAds4() {
        return getAds(TimeUnit.MILLISECONDS.convert(4, TimeUnit.HOURS), false);
    }

    public List<AdEntry> getBottomAds1() {
        return getAds(TimeUnit.MILLISECONDS.convert(1, TimeUnit.HOURS), false);
    }

    public List<AdEntry> getBottomAdsMinute() {
        return getAds(TimeUnit.MILLISECONDS.convert(1, TimeUnit.MINUTES), false);
    }

    public List<AdEntry> getAds(long duration, boolean top) {
        Pageable pageable = new PageRequest(0, statisticsCount);
        DateTime enddate = new DateTime(System.currentTimeMillis());
        DateTime startdate = enddate.minus(duration);
        Page<AdEntry> entryPage;
        if (top) {
            entryPage = repository.getTopAdsInDateRange(startdate, enddate, pageable);
        } else {
            entryPage = repository.getBottomAdsInDateRange(startdate, enddate, pageable);
        }
        return entryPage.getContent();
    }

}
