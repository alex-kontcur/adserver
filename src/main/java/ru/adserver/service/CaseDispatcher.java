package ru.adserver.service;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.Future;

/**
 * UriDispatcher
 *
 * @author Kontsur Alex (bona)
 * @since 12.11.2015
 */
@SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
@Service
public class CaseDispatcher {

    @Inject
    private List<CaseHandler> handlers;

    @Async("adExecutor")
    public Future<Pair<String, String>> processRequest(String uri) {
        for (CaseHandler handler : handlers) {
            if (handler.canHandle(uri)) {
                return new AsyncResult(handler.applyCase(uri));
            }
        }
        throw new IllegalStateException("No handler found for case \"" + uri + "\"");
    }

}
