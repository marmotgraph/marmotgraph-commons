package org.marmotgraph.commons.api;

import org.marmotgraph.commons.CommonConfig;
import org.marmotgraph.commons.MatomoConfig;
import org.marmotgraph.commons.SentryConfig;
import org.marmotgraph.commons.controller.CoreController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RequestMapping("${org.marmotgraph.api.root:}/config")
@RestController
public class Config {

    private final CoreController coreController;
    private final CommonConfig commonConfig;
    private final SentryConfig sentryConfig;
    private final MatomoConfig matomoConfig;


    public Config(CoreController coreController, CommonConfig commonConfig, SentryConfig sentryConfig, MatomoConfig matomoConfig) {
        this.coreController = coreController;
        this.commonConfig = commonConfig;
        this.sentryConfig = sentryConfig;
        this.matomoConfig = matomoConfig;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getConfig() {
        Map<String, Object> authenticationInformation = this.coreController.getAuthenticationInformation();
        if (authenticationInformation != null) {
            Object issuer = authenticationInformation.get("issuer");
            Map<String, Object> config = new HashMap<>();
            if (issuer instanceof String) {
                String[] split = ((String) issuer).split("/realms/");
                String base = split[0];
                String realm = split[split.length - 1];
                config.put("keycloak", Map.of("realm", realm, "url", base, "clientId", authenticationInformation.get("loginClientId")));
            }
            Map<String, Object> tenantInformation = this.coreController.getTenantInformation();
            if (tenantInformation != null) {
                config.put("tenant", tenantInformation);
            }
            String commit = null;
            if (this.commonConfig.getCommit() != null && !this.commonConfig.getCommit().isBlank()) {
                commit = this.commonConfig.getCommit();
                config.put("commit", commit);
            }
            if (this.sentryConfig.getDsn() != null) {
                config.put("sentry", Map.of(
                        "dsn", this.sentryConfig.getDsn(),
                        "release", commit,
                        "environment", this.sentryConfig.getEnvironment()
                ));
            }
            if (this.matomoConfig.getUrl() != null) {
                config.put("matomo", Map.of("url", this.matomoConfig.getUrl(), "siteId", this.matomoConfig.getSiteId()));
            }
            return ResponseEntity.ok(config);
        }
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }
}
