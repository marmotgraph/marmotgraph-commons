package org.marmotgraph.commons;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MatomoConfig {

    private final String url;
    private final String siteId;

    public MatomoConfig(@Value("${matomo.url:}") String url, @Value("${matomo.siteId:}") String siteId) {
        this.url = url.isBlank() ? null : url;
        this.siteId = siteId.isBlank() ? null : siteId;
    }

    public String getUrl() {
        return url;
    }

    public String getSiteId() {
        return siteId;
    }
}
