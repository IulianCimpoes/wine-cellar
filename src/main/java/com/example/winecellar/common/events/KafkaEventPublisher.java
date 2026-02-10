package com.example.winecellar.common.events;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Profile({"dev", "prod"})
@ConditionalOnMissingBean(DomainEventPublisher.class)
public class KafkaEventPublisher implements DomainEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final EventProperties props;

    public KafkaEventPublisher(KafkaTemplate<String, Object> kafkaTemplate, EventProperties props) {
        this.kafkaTemplate = kafkaTemplate;
        this.props = props;
    }

    public void publish(String key, Object event) {
        kafkaTemplate.send(props.topic(), key, event);
    }
}
