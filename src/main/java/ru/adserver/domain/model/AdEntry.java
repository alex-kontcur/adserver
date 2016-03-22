package ru.adserver.domain.model;

import com.google.gson.annotations.Expose;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import java.util.Objects;

/**
 * AdEntry
 *
 * @author Kontsur Alex (bona)
 * @since 12.11.2015
 */
public class AdEntry {

    private long id;

    @Expose
    private int hits;

    @Expose
    private String num;

    @Type(type = "ru.adserver.infrastructure.hibernate.DefaultPersistentDateTime")
    private DateTime created;

    @Expose
    @Type(type = "ru.adserver.infrastructure.hibernate.DefaultPersistentDateTime")
    private DateTime lastAccess;

    public AdEntry() {
    }

    public AdEntry(String num) {
        this.num = num;
        created = new DateTime();
    }

    public long getId() {
        return id;
    }

    public DateTime getCreated() {
        return created;
    }

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

    @Override
    public String toString() {
        return "AdEntry" + "{num=" + num + ", hits=" + hits + ", lastAccess=" + new DateTime(lastAccess) + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AdEntry adEntry = (AdEntry) o;
        return Objects.equals(id, adEntry.id) &&
            Objects.equals(num, adEntry.num);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, num);
    }
}
