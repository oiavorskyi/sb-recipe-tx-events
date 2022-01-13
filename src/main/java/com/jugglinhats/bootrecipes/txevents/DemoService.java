package com.jugglinhats.bootrecipes.txevents;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DemoService {

    private final ActivitiesCollector activitiesCollector;
    private final TransactionalCache transactionalCache;
    private final CacheConsumer cacheConsumer;

    public DemoService(ActivitiesCollector activitiesCollector, TransactionalCache transactionalCache, CacheConsumer cacheConsumer) {
        this.activitiesCollector = activitiesCollector;
        this.transactionalCache = transactionalCache;
        this.cacheConsumer = cacheConsumer;
    }

    @Transactional
    public void executeWithSucceedingTransaction() {
        doExecute(false);
    }

    @Transactional
    public void executeWithFailingTransaction() {
        doExecute(true);
    }

    public void executeWithNoTransaction() {
        doExecute(false);
    }

    private void doExecute(boolean shouldFail) {
        activitiesCollector.reportActivity("Starting new business transaction");
        transactionalCache.store("aKey", "aValue");
        cacheConsumer.doSomethingWithCache();

        if (shouldFail) {
            activitiesCollector.reportActivity("Failing business transaction");
            throw new RuntimeException("Oops");
        } else {
            activitiesCollector.reportActivity("Successfully completing business transaction");
        }
    }
}
