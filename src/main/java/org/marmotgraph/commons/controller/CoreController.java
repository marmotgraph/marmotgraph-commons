package org.marmotgraph.commons.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.marmotgraph.commons.CommonConfig;
import org.marmotgraph.commons.models.FileResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

@Component
public class CoreController {

    public static final String ASSET_CACHE = "assets";
    public static final String AUTHENTICATION_CACHE = "authentication";
    public static final String TENANT_INFORMATION_CACHE = "tenantInformation";

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final HttpClient httpClient;
    private final CommonConfig commonConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CoreController(CommonConfig commonConfig) {
        this.httpClient = HttpClient.newHttpClient();
        ;
        this.commonConfig = commonConfig;
    }

    @CachePut(value = ASSET_CACHE, unless = "#result=null")
    public FileResponse refreshAsset(String asset, boolean darkMode) {
        return loadFromCore(buildTenantAssetsUrl(asset, darkMode));
    }

    @Cacheable(value = ASSET_CACHE)
    public FileResponse readAsset(String asset, boolean darkMode) {
        return loadFromCore(buildTenantAssetsUrl(asset, darkMode));
    }

    @Cacheable(value = AUTHENTICATION_CACHE)
    public Map<String, Object> getAuthenticationInformation() {
        try {
            EndpointInformation endpointInformation = getEndpointInformation();
            Map<String, Object> result = new HashMap<>();
            if (endpointInformation != null && endpointInformation.getEndpoint() != null) {
                Map<String, Object> wellKnown = getWellKnown(endpointInformation.getEndpoint());
                if (wellKnown != null) {
                    result.putAll(wellKnown);
                }
            }
            if (endpointInformation != null && endpointInformation.getLoginClientId() != null) {
                result.put("loginClientId", endpointInformation.getLoginClientId());
            }
            return result.isEmpty() ? null : result;
        } catch (URISyntaxException | IOException | InterruptedException e) {
            logger.error("Was not able to read authentication information from core", e);
        }
        return null;
    }

    private Map<String, Object> getWellKnown(String wellKnownUrl) throws URISyntaxException, IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(new URI(wellKnownUrl)).GET().build();
        HttpResponse<InputStream> response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
        if (response != null && response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), Map.class);
        }
        return null;
    }

    private final static class EndpointInformation {
        private final String endpoint;
        private final String loginClientId;

        public EndpointInformation(String endpoint, String loginClientId) {
            this.endpoint = endpoint;
            this.loginClientId = loginClientId;
        }

        public String getEndpoint() {
            return endpoint;
        }

        public String getLoginClientId() {
            return loginClientId;
        }
    }

    private EndpointInformation getEndpointInformation() throws URISyntaxException, IOException, InterruptedException {
        String url = String.format("%s/setup/authentication", buildCoreRootUrl());
        logger.info("Loading authentication information from core ({})", url);
        HttpRequest request = HttpRequest.newBuilder(new URI(url)).GET().build();
        HttpResponse<InputStream> response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
        if (response != null && response.statusCode() == 200) {
            Map<String, Object> resultAsMap = objectMapper.readValue(response.body(), Map.class);
            if (resultAsMap != null) {
                Object data = resultAsMap.get("data");
                if (data instanceof Map) {
                    Object endpoint = ((Map<?, ?>) data).get("endpoint");
                    Object loginClientId = ((Map<?, ?>) data).get("loginClientId");
                    return new EndpointInformation(endpoint instanceof String ? (String) endpoint : null, loginClientId instanceof String ? (String) loginClientId : null);
                }
            }
        }
        return null;
    }

    @Cacheable(value = TENANT_INFORMATION_CACHE)
    public Map<String, Object> getTenantInformation() {
        try {
            String url = buildTenantUrl();
            logger.info("Loading tenant information from core ({})", url);
            HttpResponse<InputStream> response = this.httpClient.send(HttpRequest.newBuilder(new URI(url)).GET().build(), HttpResponse.BodyHandlers.ofInputStream());
            if (response != null && response.statusCode() == 200) {
                return objectMapper.readValue(response.body(), Map.class);
            }
        } catch (URISyntaxException | IOException | InterruptedException e) {
            logger.error("Was not able to read tenant information from core", e);
        }
        return null;
    }


    private String buildCoreRootUrl() {
        return String.format("https://%s/%s", commonConfig.getHostName(), commonConfig.getApiVersion());
    }

    private String buildTenantUrl() {
        return String.format("%s/tenants/%s", buildCoreRootUrl(), commonConfig.getTenant());
    }

    private String buildTenantAssetsUrl(String asset, boolean darkMode) {
        return String.format("%s/theme/%s?darkMode=%s", buildTenantUrl(), asset, darkMode);
    }

    private FileResponse loadFromCore(String url) {
        try {
            logger.info("Loading asset {} from core", url);
            HttpRequest request = HttpRequest.newBuilder(new URI(url)).GET().build();
            HttpResponse<byte[]> response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() == 200) {
                return new FileResponse(response.body(), response.headers().map());
            } else {
                logger.warn("Was not able to load asset {} from core - status code {}", url, response.statusCode());
            }
            return null;
        } catch (URISyntaxException | InterruptedException | IOException e) {
            logger.warn("Was not able to load asset {} from core", url, e);
            return null;
        }
    }

}
