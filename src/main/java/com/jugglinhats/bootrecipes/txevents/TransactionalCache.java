package com.jugglinhats.bootrecipes.txevents;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
class TransactionalCache {

    public static final String TX_CACHE_KEY = "the-tx-cache";

    private final ApplicationEventPublisher eventPublisher;
    private final ActivitiesCollector activitiesCollector;

    TransactionalCache(ApplicationEventPublisher eventPublisher, ActivitiesCollector activitiesCollector) {
        this.eventPublisher = eventPublisher;
        this.activitiesCollector = activitiesCollector;
    }

    void store(String key, Object value) {
        activitiesCollector.reportActivity(
                String.format("Attempting to store data to transactional cache with key [%s] and value [%s]", key, value));
        tryGetCache().ifPresent(cache -> cache.put(key, value));
    }

    Object retrieve(String key) {
        return getCache()
                .map(cache -> cache.get(key))
                .orElse(null);
    }

    @SuppressWarnings("unchecked")
    private Optional<Map<String, Object>> getCache() {
        return Optional.ofNullable(TransactionSynchronizationManager.getResource(TX_CACHE_KEY))
                .map(Map.class::cast);
    }

    private Optional<Map<String, Object>> tryGetCache() {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            activitiesCollector.reportActivity("Skipping transactional cache initialization - no active transaction");
            return Optional.empty();
        }

        Map<String, Object> cache;

        if (!TransactionSynchronizationManager.hasResource(TX_CACHE_KEY)) {
            activitiesCollector.reportActivity("Initializing transactional cache");
            cache = new ConcurrentHashMap<>();
            TransactionSynchronizationManager.bindResource(TX_CACHE_KEY, cache);
            eventPublisher.publishEvent(new TransactionalCacheCreatedEvent(cache));
        } else {
            cache = (Map<String, Object>) TransactionSynchronizationManager.getResource(TX_CACHE_KEY);
        }

        return Optional.ofNullable(cache);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void afterCommitWithContext(TransactionalCacheCreatedEvent event) {
        activitiesCollector.reportActivity("Flushing transactional cache upon transaction commit");
        TransactionSynchronizationManager.unbindResourceIfPossible(TX_CACHE_KEY);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void afterRollbackWithContext(TransactionalCacheCreatedEvent event) {
        activitiesCollector.reportActivity("Discarding transactional cache upon transaction rollback");
        TransactionSynchronizationManager.unbindResourceIfPossible(TX_CACHE_KEY);
    }

    record TransactionalCacheCreatedEvent(Map<String, Object> cache) {
    }
}
