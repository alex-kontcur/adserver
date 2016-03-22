package ru.adserver.infrastructure;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParseException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.util.Assert;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

/**
 * GsonHttpMessageConverter
 *
 * @author Kontcur Alex (bona)
 * @since 14.11.2015
 */
public class GsonHttpMessageConverter extends AbstractHttpMessageConverter<Object> {

    public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    private Gson gson;

    private Type type;

    private boolean prefixJson;


    /**
     * Construct a new {@code GsonHttpMessageConverter} with a default {@link Gson#Gson() Gson}.
     */
    public GsonHttpMessageConverter() {
        this(new Gson());
    }

    /**
     * Construct a new {@code GsonHttpMessageConverter}.
     *
     * @param serializeNulls true to generate json for null values
     */
    public GsonHttpMessageConverter(boolean serializeNulls) {
        this(serializeNulls ? new GsonBuilder().serializeNulls().create() : new Gson());
    }

    /**
     * Construct a new {@code GsonHttpMessageConverter}.
     *
     * @param gson a customized {@link Gson#Gson() Gson}
     */
    public GsonHttpMessageConverter(Gson gson) {
        super(new MediaType("application", "json", DEFAULT_CHARSET));
        setGson(gson);
    }

    /**
     * Sets the {@code Gson} for this view. If not set, a default
     * {@link Gson#Gson() Gson} is used.
     * <p>Setting a custom-configured {@code Gson} is one way to take further control of the JSON serialization
     * process.
     *
     * @throws IllegalArgumentException if gson is null
     */
    public void setGson(Gson gson) {
        Assert.notNull(gson, "'gson' must not be null");
        this.gson = gson;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    /**
     * Indicates whether the JSON output by this view should be prefixed with "{} &&". Default is false.
     * <p> Prefixing the JSON string in this manner is used to help prevent JSON Hijacking. The prefix renders the string
     * syntactically invalid as a script so that it cannot be hijacked. This prefix does not affect the evaluation of JSON,
     * but if JSON validation is performed on the string, the prefix would need to be ignored.
     */
    public void setPrefixJson(boolean prefixJson) {
        this.prefixJson = prefixJson;
    }

    @Override
    public boolean canRead(Class<?> clazz, MediaType mediaType) {
        return canRead(mediaType);
    }

    @Override
    public boolean canWrite(Class<?> clazz, MediaType mediaType) {
        return canWrite(mediaType);
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        // should not be called, since we override canRead/Write instead
        throw new UnsupportedOperationException();
    }

    @Override
    protected Object readInternal(Class<?> clazz, HttpInputMessage inputMessage) throws IOException {
        try (Reader json = new InputStreamReader(inputMessage.getBody(), getCharset(inputMessage.getHeaders()))) {
            Type typeOfT = type;
            if (typeOfT != null) {
                return gson.fromJson(json, typeOfT);
            } else {
                return gson.fromJson(json, clazz);
            }
        } catch (JsonParseException ex) {
            throw new HttpMessageNotReadableException("Could not read JSON: " + ex.getMessage(), ex);
        }
    }

    @Override
    protected void writeInternal(Object o, HttpOutputMessage outputMessage) throws IOException {
        OutputStreamWriter writer = new OutputStreamWriter(outputMessage.getBody(), getCharset(outputMessage.getHeaders()));
        try {
            if (prefixJson) {
                writer.append("{} && ");
            }
            Type typeOfSrc = type;
            if (typeOfSrc != null) {
                gson.toJson(o, typeOfSrc, writer);
            } else {
                gson.toJson(o, writer);
            }
        } catch (JsonIOException ex) {
            throw new HttpMessageNotWritableException("Could not write JSON: " + ex.getMessage(), ex);
        }
    }

    private static Charset getCharset(HttpHeaders headers) {
        if (headers != null && headers.getContentType() != null
            && headers.getContentType().getCharSet() != null) {
            return headers.getContentType().getCharSet();
        }
        return DEFAULT_CHARSET;
    }

}