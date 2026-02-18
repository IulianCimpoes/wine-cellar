package com.example.winecellar.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, String> {
    List<OutboxEvent> findTop50ByStatusOrderByOccurredAtAsc(String status);

    @Query("""
    select e from OutboxEvent e
    where (e.status = 'NEW' or e.status = 'FAILED')
      and (e.nextAttemptAt is null or e.nextAttemptAt <= :now)
    order by e.occurredAt asc
""")
    List<OutboxEvent> findEligible(@Param("now") Instant now);
}
