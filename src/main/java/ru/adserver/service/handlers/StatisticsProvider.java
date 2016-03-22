package ru.adserver.service.handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;
import ru.adserver.domain.model.AdEntry;
import ru.adserver.infrastructure.hibernate.DateTimeTypeAdapter;
import ru.adserver.service.CaseHandler;
import ru.adserver.service.StatManager;

import javax.inject.Inject;
import java.util.List;

/**
 * StatisticsProvider
 *
 * @author Kontsur Alex (bona)
 * @since 12.11.2015
 */
@Service
public class StatisticsProvider implements CaseHandler {

    private static final String URI_PREFIX = "/stat";

    @Inject
    private StatManager statManager;

    private Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting()
        .registerTypeAdapter(DateTime.class, new DateTimeTypeAdapter()).create();

    @Override
    public Pair<String, String> applyCase(String uri) {
        int len = URI_PREFIX.length();
        String statKind = uri.substring(len, len + 1);
        String duration = uri.substring(len + 2);
        switch (statKind) {
            case "1": return calcStat1(duration);
            case "2": return calcStat2(duration);
            default: throw new IllegalArgumentException("Not implemented");
        }
    }

    private Pair<String, String> calcStat1(String duration) {
        List<AdEntry> ads;
        switch (duration) {
            case "24":
                ads = statManager.getTopAds24();
                break;
            case "4":
                ads = statManager.getTopAds4();
                break;
            case "1":
                ads = statManager.getTopAds1();
                break;
            case "1m":
                ads = statManager.getTopAdsMinute();
                break;
            default:
                throw new IllegalArgumentException("Not implemented");
        }
        return Pair.of(gson.toJson(ads), "application/json");
    }

    private Pair<String, String> calcStat2(String duration) {
        List<AdEntry> ads;
        switch (duration) {
            case "24":
                ads = statManager.getBottomAds24();
                break;
            case "4":
                ads = statManager.getBottomAds4();
                break;
            case "1":
                ads = statManager.getBottomAds1();
                break;
            case "1m":
                ads = statManager.getBottomAdsMinute();
                break;
            default:
                throw new IllegalArgumentException("Not implemented");
        }
        return Pair.of(gson.toJson(ads), "application/json");
    }

    @Override
    public boolean canHandle(String uri) {
        return uri.startsWith(URI_PREFIX + "1/") || uri.startsWith(URI_PREFIX + "2/");
    }
}
