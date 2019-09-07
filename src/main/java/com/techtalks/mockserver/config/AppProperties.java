package com.techtalks.mockserver.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private final Github github = new Github();

    @Setter
    @Getter
    public static class Github {
        private String username;
        private String token;
    }
}
