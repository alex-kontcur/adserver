package ru.adserver.service;

import org.apache.commons.lang3.tuple.Pair;

/**
 * CaseHandler
 *
 * @author Kontsur Alex (bona)
 * @since 12.11.2015
 */
public interface CaseHandler {

    Pair<String, String> applyCase(String uri);

    boolean canHandle(String uri);

}
