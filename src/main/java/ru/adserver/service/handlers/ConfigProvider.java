package ru.adserver.service.handlers;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import ru.adserver.infrastructure.RuntimeConfig;
import ru.adserver.service.CaseHandler;

import javax.inject.Inject;

/**
 * RuntimeConfigProvider
 *
 * @author Kontsur Alex (bona)
 * @since 13.11.2015
 */
@Service
public class ConfigProvider implements CaseHandler {

    public static final String URI_PREFIX = "/config";
    
    @Inject
    private RuntimeConfig runtimeConfig;

    @Override
    public Pair<String, String> applyCase(String uri) {
        return Pair.of(runtimeConfig.getRuntimeProperties(), "application/json");
    }

    @Override
    public boolean canHandle(String uri) {
        return uri.startsWith(URI_PREFIX);
    }
}
