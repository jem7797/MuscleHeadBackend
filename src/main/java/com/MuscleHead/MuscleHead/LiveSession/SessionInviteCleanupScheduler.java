package com.MuscleHead.MuscleHead.LiveSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SessionInviteCleanupScheduler {

    private static final Logger logger = LoggerFactory.getLogger(SessionInviteCleanupScheduler.class);

    @Autowired
    private LiveSessionService liveSessionService;

    @Scheduled(cron = "${live-session.invites.cleanup.cron:0 0 * * * *}")
    public void deleteExpiredInvites() {
        long deleted = liveSessionService.deleteExpiredInvites();
        if (deleted > 0) {
            logger.info("Live session invites cleanup: deleted {} expired invites", deleted);
        }
    }
}
