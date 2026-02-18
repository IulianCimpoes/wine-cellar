package com.example.winecellar.winery;

import com.example.winecellar.outbox.OutboxEventRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("dev")
@WithMockUser(roles = "ADMIN")
class WineryOutboxIT {

    @Autowired WineryService wineryService;
    @Autowired OutboxEventRepository outboxRepo;

    @AfterEach
    void cleanup() {
        MDC.clear();
    }

    @Test
    void createWinery_writesOutboxEvent_withRequestId_andDomainEventJson() {
        MDC.put("requestId", "req-123");

        Winery saved = wineryService.create(Winery.builder()
                .name("TestWinery")
                .country("TestCountry")
                .version(1l)
                .build()); // adapt to your actual API

        var events = outboxRepo.findTop50ByStatusOrderByOccurredAtAsc("NEW");
        assertThat(events).hasSize(1);

        var e = events.get(0);
        assertThat(e.getEventType()).isEqualTo("WineryCreated");
        assertThat(e.getAggregateId()).isEqualTo(saved.getId().toString());
        assertThat(e.getRequestId()).isEqualTo("req-123");
        assertThat(e.getPayload()).contains("\"eventType\":\"WineryCreated\"");
        assertThat(e.getPayload()).contains("\"requestId\":\"req-123\"");
        assertThat(e.getPayload()).contains(saved.getId().toString());
    }
}
