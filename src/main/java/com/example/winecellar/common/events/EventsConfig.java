package com.example.winecellar.common.events;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(EventProperties.class)
public class EventsConfig {}
