package org.marmotgraph.commons.controller;

import org.marmotgraph.commons.CommonConfig;
import org.marmotgraph.commons.models.FileResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
public class ThemeController {

    public static final String ASSET_CACHE = "assets";

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final HttpClient httpClient;

    public ThemeController() {
        this.httpClient = HttpClient.newHttpClient();;
    }

    @CachePut(value = ASSET_CACHE, unless = "#result=null", cacheManager = CommonConfig.ASSET_CACHE_MANAGER)
    public FileResponse refreshAsset(String url){
        return loadFromCore(url);
    }

    @Cacheable(value = ASSET_CACHE, cacheManager = CommonConfig.ASSET_CACHE_MANAGER)
    public FileResponse readAsset(String url) {
        return loadFromCore(url);
    }

    private FileResponse loadFromCore(String url) {
        try {
            logger.info("Loading asset {} from core", url);
            HttpRequest request = HttpRequest.newBuilder(new URI(url)).GET().build();
            HttpResponse<byte[]> response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if(response.statusCode() == 200) {
                return new FileResponse(response.body(), response.headers().map());
            }
            return null;
        }
        catch (URISyntaxException | InterruptedException | IOException e) {
            logger.warn("Was not able to load asset {} from core", url, e);
            return null;
        }
    }

}
