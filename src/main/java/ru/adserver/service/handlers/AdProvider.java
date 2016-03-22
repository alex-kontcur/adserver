package ru.adserver.service.handlers;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.adserver.infrastructure.FsReader;
import ru.adserver.service.AdsManager;
import ru.adserver.service.CaseHandler;
import ru.adserver.service.PropertyObserver;

import javax.inject.Inject;

/**
 * AdService
 *
 * @author Kontsur Alex (bona)
 * @since 12.11.2015
 */
@Service
public class AdProvider implements CaseHandler, PropertyObserver {

    private static final Logger logger = LoggerFactory.getLogger(AdProvider.class);

    private static final String ADS_PATH_PROPERTY = "ads.path";

    private static final String URI_PREFIX = "/banner/";

    @Value("${ads.path}")
    private String adsPath;

    @Inject
    private FsReader fsReader;

    @Inject
    private AdsManager adsManager;

    @Override
    public Pair<String, String> applyCase(String uri) {
        long start = System.currentTimeMillis();
        String num = uri.substring(URI_PREFIX.length());
        String adContent = null;
        if (adsManager.adExists(num)) {
            String fileName = adsPath + "/" + num + ".html";
            adContent = fsReader.read(fileName, true);
            if (adContent != null) {
                adsManager.updateAdInfo(num);

                logger.debug("Processing ad {} took {} ms", fileName, System.currentTimeMillis() - start);
            }
        }
        return Pair.of(adContent == null ? "Not found" : adContent, "text/html");
    }

    @Override
    public boolean canHandle(String uri) {
        return uri.startsWith(URI_PREFIX);
    }

    @Override
    public void propertyChanged(String name, String value) {
        if (name.endsWith(ADS_PATH_PROPERTY)) {
            adsPath = value;
        }
    }
}
