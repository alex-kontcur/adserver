package ru.adserver.service;

import org.joda.time.DateTime;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * AdInfo
 *
 * @author Kontsur Alex (bona)
 * @since 14.11.2015
 */
public class AdInfo {

    private long created;
    private AtomicInteger hits;
    private AtomicLong lassAccessTime;
    private AtomicBoolean indexed;

    public AdInfo() {
        hits = new AtomicInteger(0);
        indexed = new AtomicBoolean(false);
        lassAccessTime = new AtomicLong(0);
        created = DateTime.now().getMillis();
    }

    public AdInfo(int hits, long lassAccessTime) {
        this.hits = new AtomicInteger(hits);
        indexed = new AtomicBoolean(false);
        created = DateTime.now().getMillis();
        this.lassAccessTime = new AtomicLong(lassAccessTime);
    }

    public AtomicInteger getHits() {
        return hits;
    }

    public long getLassAccessTime() {
        return lassAccessTime.get();
    }

    public void setLassAccessTime(long lassAccessTime) {
        this.lassAccessTime.set(lassAccessTime);
    }

    public long getCreated() {
        return created;
    }

    public boolean getIndexed() {
        return indexed.get();
    }

    public void setIndexed(boolean indexed) {
        this.indexed.set(indexed);
    }
}
