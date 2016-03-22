package ru.adserver.infrastructure;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * AdsGenerator
 *
 * @author Kontcur Alex (bona)
 * @since 14.11.2015
 */
@Component
public class AdsGenerator {

    private static final Logger logger = LoggerFactory.getLogger(AdsGenerator.class);

    @Value("${ads.path}")
    private String adsPath;

    public void cleanAdsPath() {
        try {
            FileUtils.cleanDirectory(new File(adsPath));
        } catch (Exception e) {
            logger.error("Error while cleanFolder \"" + adsPath + "\" ->", e);
        }
    }

    public List<Pair<String, String>> generateAds(int count) {
        List<Pair<String, String>> nums = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Pair<String, String> pair = generateAd(i);
            nums.add(pair);
        }
        return nums;
    }

    private Pair<String, String> generateAd(int i) {
        File ad = new File(adsPath + "/" + i + ".html");
        String content = "content_" + i + " content_" + i;
        try (FileOutputStream os = new FileOutputStream(ad)) {
            try (FileChannel channel = os.getChannel()) {
                channel.write(ByteBuffer.wrap(content.getBytes()));
            }
        } catch (Exception ignored) {
        }
        return Pair.of(String.valueOf(i), content);
    }
}
