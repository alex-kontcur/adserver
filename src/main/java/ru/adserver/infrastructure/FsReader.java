package ru.adserver.infrastructure;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * AdReader
 *
 * @author Kontsur Alex (bona)
 * @since 14.11.2015
 */
@Component
public class FsReader {

    @Cacheable(value = "fs-cache", condition = "#bannerProcessing == true", unless="#result == null")
    public String read(String fileName, boolean bannerProcessing) {
        try (FileInputStream stream = new FileInputStream(fileName)) {
            try (FileChannel channel = stream.getChannel()) {
                ByteBuffer buffer = ByteBuffer.allocate(stream.available());
                channel.read(buffer);
                return new String(buffer.array());
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
