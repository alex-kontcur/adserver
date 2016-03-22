package ru.adserver.infrastructure;

import org.joda.time.DateTime;

/**
 * StatResponse
 *
 * @author Kontcur Alex (bona)
 * @since 14.11.2015
 */
public class StatResponse {

    private String num;
    private int hits;
    private DateTime lastAccess;

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public int getHits() {
        return hits;
    }

    public void setHits(int hits) {
        this.hits = hits;
    }

    public DateTime getLastAccess() {
        return lastAccess;
    }

    public void setLastAccess(DateTime lastAccess) {
        this.lastAccess = lastAccess;
    }
}
