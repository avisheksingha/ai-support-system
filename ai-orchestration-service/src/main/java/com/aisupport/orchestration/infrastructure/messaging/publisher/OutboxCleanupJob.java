package com.aisupport.orchestration.infrastructure.messaging.publisher;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.aisupport.orchestration.infrastructure.persistence.repository.OutboxEventRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxCleanupJob {

    private final OutboxEventRepository repository;

    @Scheduled(cron = "${governance.outbox.cleanup.cron:0 0 2 * * *}")
    @Transactional
    public void cleanupSentEvents() {

        Instant cutoff = Instant.now().minus(7, ChronoUnit.DAYS);

        int deleted = repository.deleteSentEventsOlderThan(cutoff);

        if (deleted > 0) {
            log.info("Deleted {} old outbox events", deleted);
        }
    }
}
