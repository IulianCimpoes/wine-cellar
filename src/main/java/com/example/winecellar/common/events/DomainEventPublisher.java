package com.example.winecellar.common.events;

public interface DomainEventPublisher {
    void publish(String key, Object event);
}
