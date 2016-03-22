package ru.adserver.infrastructure.hibernate;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.joda.time.DateTime;

import java.io.IOException;

/**
 * DateTimeTypeAdapter
 *
 * @author Kontsur Alex (bona)
 * @since 14.11.2015
 */
public class DateTimeTypeAdapter extends TypeAdapter<DateTime> {

    @Override
    public void write(JsonWriter out, DateTime value) throws IOException {
        String time = value != null ? value.toString() : null;
        out.beginObject();
        if (value != null) {
            out.name("dt").value(time);
        }
        out.endObject();
    }

    @Override
    public DateTime read(JsonReader in) throws IOException {
        Long millis = null;
        in.beginObject();
        while (in.hasNext()) {
            String name = in.nextName();
            if (name.equals("dt")) {
                millis = in.nextLong();
                break;
            } else {
                in.skipValue();
            }
        }
        in.endObject();
        return millis != null ? new DateTime(millis) : null;
    }

}