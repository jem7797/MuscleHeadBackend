package com.MuscleHead.MuscleHead.WorkedMuscles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class WorkedMusclesCleanupScheduler {

    private static final Logger logger = LoggerFactory.getLogger(WorkedMusclesCleanupScheduler.class);

    @Autowired
    private WorkedMusclesService workedMusclesService;

    @Scheduled(cron = "${worked-muscles.cleanup.cron:0 0 3 * * *}")
    public void deleteExpiredWorkedMuscles() {
        int deleted = workedMusclesService.deleteExpiredRows();
        if (deleted > 0) {
            logger.info("Worked muscles cleanup: deleted {} expired rows", deleted);
        }
    }
}
