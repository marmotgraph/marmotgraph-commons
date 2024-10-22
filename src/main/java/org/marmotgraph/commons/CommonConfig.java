package org.marmotgraph.commons;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@EnableCaching
@ComponentScan
@EnableAutoConfiguration
@PropertySource({"classpath:common.properties"})
public class CommonConfig {

    private final String tenant;
    private final String hostName;
    private final String apiVersion;
    private final String commit;
    private final String componentName;
    private final String clientId;
    private final String clientSecret;

    public CommonConfig(@Value("${org.marmotgraph.component.name}") String componentName, @Value("${org.marmotgraph.component.clientId}") String clientId, @Value("${org.marmotgraph.component.clientSecret}") String clientSecret, @Value("${org.marmotgraph.tenant:default}") String tenant, @Value("${org.marmotgraph.core.host}") String hostName, @Value("${org.marmotgraph.core.apiVersion:v3}") String apiVersion, @Value("${org.marmotgraph.commit:unknown}") String commit) {
        this.tenant = tenant;
        this.hostName = hostName;
        this.apiVersion = apiVersion;
        this.commit = commit;
        this.componentName = componentName;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public String getComponentName() {
        return componentName;
    }

    public String getTenant() {
        return tenant;
    }

    public String getHostName() {
        return hostName;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public String getCommit() {
        return commit;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }
}
