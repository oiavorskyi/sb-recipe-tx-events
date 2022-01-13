package com.jugglinhats.bootrecipes.txevents;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

@Service
final class ActivitiesCollector {
    private final static Logger log = LoggerFactory.getLogger(ActivitiesCollector.class);

    private final List<Activity> activities = new ArrayList<>();

    void reportActivity(String description) {
        log.info("Reported activity: '{}'", description);
        activities.add(new Activity(description));
    }

    List<String> getReportedActivities() {
        return activities.stream()
                .map(Activity::description)
                .toList();
    }

    void removeAll() {
        activities.clear();
    }

    private record Activity(String description) {
    }
}
