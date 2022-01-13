package com.jugglinhats.bootrecipes.txevents;

import org.springframework.stereotype.Service;

@Service
class CacheConsumer {

    private final ActivitiesCollector activitiesCollector;
    private final TransactionalCache transactionalCache;

    CacheConsumer(ActivitiesCollector activitiesCollector, TransactionalCache transactionalCache) {
        this.activitiesCollector = activitiesCollector;
        this.transactionalCache = transactionalCache;
    }

    void doSomethingWithCache() {
        var cacheKey = "aKey";
        activitiesCollector.reportActivity(
                String.format("Retrieving data from transactional cache with key [%s]: result is [%s]",
                        cacheKey, transactionalCache.retrieve(cacheKey)));
    }

}
