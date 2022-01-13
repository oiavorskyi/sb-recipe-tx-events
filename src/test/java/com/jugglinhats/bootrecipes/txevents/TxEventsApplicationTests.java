package com.jugglinhats.bootrecipes.txevents;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class TxEventsApplicationTests {

    @Autowired
    private DemoService demoService;

    @Autowired
    private ActivitiesCollector activitiesCollector;

    @BeforeEach
    void cleanupActivities() {
        activitiesCollector.removeAll();
    }


    @Test
    void flushesTxCacheUponTransactionCommit() {
        demoService.executeWithSucceedingTransaction();

        assertThat(activitiesCollector.getReportedActivities())
                .containsExactly(
                        "Starting new business transaction",
                        "Attempting to store data to transactional cache with key [aKey] and value [aValue]",
                        "Initializing transactional cache",
                        "Retrieving data from transactional cache with key [aKey]: result is [aValue]",
                        "Successfully completing business transaction",
                        "Flushing transactional cache upon transaction commit"
                );
    }

    @Test
    void discardsTxCacheWhenTransactionRollsBack() {
        assertThatThrownBy(() -> demoService.executeWithFailingTransaction())
                .hasMessage("Oops");

        assertThat(activitiesCollector.getReportedActivities())
                .containsExactly(
                        "Starting new business transaction",
                        "Attempting to store data to transactional cache with key [aKey] and value [aValue]",
                        "Initializing transactional cache",
                        "Retrieving data from transactional cache with key [aKey]: result is [aValue]",
                        "Failing business transaction",
                        "Discarding transactional cache upon transaction rollback"
                );

    }

    @Test
    void ignoresTxCacheWhenNotInTransaction() {
        demoService.executeWithNoTransaction();

        assertThat(activitiesCollector.getReportedActivities())
                .containsExactly(
                        "Starting new business transaction",
                        "Attempting to store data to transactional cache with key [aKey] and value [aValue]",
                        "Skipping transactional cache initialization - no active transaction",
                        "Retrieving data from transactional cache with key [aKey]: result is [null]",
                        "Successfully completing business transaction"
                );
    }
}
