package com.example.winecellar.common.events;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.events")
public record EventProperties(String topic) {}
