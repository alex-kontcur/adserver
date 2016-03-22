package ru.adserver.infrastructure;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;

/**
 * RestClient
 *
 * @author Kontcur Alex (bona)
 * @since 14.11.2015
 */
@Component
public class RestClient {

    @Inject
    private RestTemplate restTemplate;

    @Value("${adserver.port}")
    private int adserverPort;

    @Async("restExecutor")
    public void getBanner(String num, ProcessingCallback callback) throws Exception {
        String url = "http://localhost:" + adserverPort + "/banner/" + num;
        HttpHeaders httpHeaders = fillHeaders("application/json", null, "localhost", true);
        HttpEntity<String> numEntity = new HttpEntity<>(httpHeaders);
        String banner = null;
        try {
            ResponseEntity<String> entity = restTemplate.exchange(url, HttpMethod.GET, numEntity, String.class);
            HttpStatus statusCode = entity.getStatusCode();
            banner = statusCode.equals(HttpStatus.OK) ? entity.getBody() : "ERROR";
        } finally {
            callback.onComplete(banner);
        }
    }

    public StatResponse[] getStat() throws Exception {
        String url = "http://localhost:" + adserverPort + "/stat1/4";
        HttpHeaders httpHeaders = fillHeaders("application/json", null, "localhost", true);
        HttpEntity<String> numEntity = new HttpEntity<>(httpHeaders);

        ResponseEntity<StatResponse[]> entity = restTemplate.exchange(url, HttpMethod.GET, numEntity, StatResponse[].class);
        HttpStatus statusCode = entity.getStatusCode();
        return statusCode.equals(HttpStatus.OK) ? entity.getBody() : new StatResponse[0];
    }

    protected static HttpHeaders fillHeaders(String accept, String contentType, String host, boolean expect100, Integer... length) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Host", host);
        httpHeaders.set("Accept", accept);
        if (length.length > 0) {
            httpHeaders.set("Content-Length", String.valueOf(length[0]));
        }
        if (expect100) {
            httpHeaders.set("Expect", "100-continue");
        }
        if (contentType != null) {
            httpHeaders.set("Content-Type", contentType);
        } else {
            httpHeaders.set("Content-Type", "text/plain; charset=utf-8");
        }
        httpHeaders.set("Connection", "Keep-Alive");
        return httpHeaders;
    }

}
