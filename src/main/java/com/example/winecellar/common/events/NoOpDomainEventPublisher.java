package com.example.winecellar.common.events;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
public class NoOpDomainEventPublisher implements DomainEventPublisher {

    @Override
    public void publish(String key, Object event) {
        // intentionally no-op
    }
}
