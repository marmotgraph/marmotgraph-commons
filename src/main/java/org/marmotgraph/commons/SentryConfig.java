package org.marmotgraph.commons;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SentryConfig {

    private final String environment;
    private final String dsn;

    public SentryConfig(@Value("${sentry.environment:unknown}") String environment, @Value("${sentry.dsn:}") String dsn) {
        this.environment = environment.isBlank() ? null : environment;
        this.dsn = dsn.isBlank() ? null : dsn;
    }

    public String getEnvironment() {
        return environment;
    }

    public String getDsn() {
        return dsn;
    }
}
