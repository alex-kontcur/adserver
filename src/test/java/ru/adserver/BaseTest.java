package ru.adserver;

import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.adserver.domain.model.AdEntry;
import ru.adserver.domain.repository.AdRepository;
import ru.adserver.infrastructure.AdsGenerator;
import ru.adserver.infrastructure.ProcessingCallback;
import ru.adserver.infrastructure.RestClient;
import ru.adserver.infrastructure.StatResponse;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * BaseTest
 *
 * @author Kontsur Alex (bona)
 * @since 14.11.2015
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:META-INF/spring/server-test-context.xml")
public class BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(BaseTest.class);

    @Inject
    private AdRepository adRepository;

    @Inject
    private AdsGenerator adsGenerator;

    @Inject
    private RestClient restClient;

    @Value("${adserver.port}")
    private int adserverPort;

    @Before
    public void init() {
        adRepository.deleteAll();
    }

    @Test
    public void testServerLoading() throws Exception {
        TimeUnit.SECONDS.sleep(5);

        int count = 10000;
        List<Pair<String, String>> numPairs = adsGenerator.generateAds(count);

        TimeUnit.SECONDS.sleep(1);

        final CountDownLatch latch = new CountDownLatch(count);
        long start = System.currentTimeMillis();
        for (Pair<String, String> pair : numPairs) {
            String num = pair.getKey();
            final String content = pair.getValue();
            restClient.getBanner(num, new ProcessingCallback() {
                @Override
                public void onComplete(String banner) {
                    latch.countDown();
                    Assert.assertTrue(banner.contains(content.split(" ")[0]));
                }
            });
        }
        latch.await(5, TimeUnit.MINUTES);

        logger.info("");
        logger.info("Getting {} ads took {} ms", count, System.currentTimeMillis() - start);
        logger.info("");
    }

    @Test @Ignore
    public void testStatisticsResponse() throws Exception {
        TimeUnit.SECONDS.sleep(5); //scheduler for initing ads starts in 3 seconds
        List<Pair<String, String>> numPairs = adsGenerator.generateAds(1);
        TimeUnit.SECONDS.sleep(1);
        for (Pair<String, String> pair : numPairs) {
            String num = pair.getKey();
            final String content = pair.getValue();
            restClient.getBanner(num, new ProcessingCallback() {
                @Override
                public void onComplete(String banner) {
                    Assert.assertTrue(banner.contains(content.split(" ")[0]));
                }
            });
        }
        TimeUnit.SECONDS.sleep(8);
        StatResponse[] responses = restClient.getStat();
        Assert.assertEquals(2, responses[0].getHits()); // +1 from testServerLoading test
    }

    @Test
    public void testStatQueryWorkingCorrect() {
        DateTime start = new DateTime(System.currentTimeMillis());

        AdEntry adEntry = new AdEntry("1");
        adEntry.setHits(55);
        adRepository.save(adEntry);

        adEntry = new AdEntry("2");
        adEntry.setHits(23);
        adRepository.save(adEntry);

        adEntry = new AdEntry("3");
        adEntry.setHits(1);
        adRepository.save(adEntry);

        adEntry = new AdEntry("4");
        adEntry.setHits(78);
        adRepository.save(adEntry);

        adEntry = new AdEntry("5");
        adEntry.setHits(556);
        adRepository.save(adEntry);

        int[] maxArr = {556, 78, 55};
        int i = 0;
        Page<AdEntry> entryPage = adRepository.getTopAdsInDateRange(start, start.plus(10000), new PageRequest(0, 3));
        for (AdEntry entry : entryPage) {
            Assert.assertEquals(entry.getHits(), maxArr[i++]);
        }

        int[] minArr = {1, 23, 55};
        i = 0;
        entryPage = adRepository.getBottomAdsInDateRange(start, start.plus(10000), new PageRequest(0, 3));
        for (AdEntry entry : entryPage) {
            Assert.assertEquals(entry.getHits(), minArr[i++]);
        }
    }

}
