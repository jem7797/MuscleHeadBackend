package com.MuscleHead.MuscleHead.Notification;

import java.time.format.DateTimeFormatter;
import java.time.ZoneId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.MuscleHead.MuscleHead.User.User;
import com.MuscleHead.MuscleHead.cache.RedisService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    private static final String CACHE_PREFIX = "notification:";

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private RedisService redisService;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${notification.cache.ttl-seconds:90}")
    private int cacheTtlSeconds;

    public Notification createNotification(User user, NotificationType type, String message) {
        Notification n = new Notification();
        n.setUser(user);
        n.setType(type);
        n.setMessage(message);
        Notification saved = notificationRepository.save(n);
        invalidateCacheForUser(user.getSub_id());
        return saved;
    }

    /**
     * Creates a MEDAL_EARNED notification with medal info for the frontend.
     */
    public Notification createMedalNotification(User user, com.MuscleHead.MuscleHead.Medal.UserMedal medal, String message) {
        Notification n = new Notification();
        n.setUser(user);
        n.setType(NotificationType.MEDAL_EARNED);
        n.setMessage(message);
        n.setMedalId(medal.getId());
        n.setMedalName(medal.getMedalName() != null ? medal.getMedalName().name() : null);
        Notification saved = notificationRepository.save(n);
        invalidateCacheForUser(user.getSub_id());
        return saved;
    }

    public void createFollowNotification(User followee, User follower) {
        String message = follower.getUsername() + " started following you";
        createNotification(followee, NotificationType.FOLLOW, message);
    }

    public Page<NotificationResponse> getNotificationsForUser(String subId, Pageable pageable) {
        String sortKey = pageable.getSort().isSorted() ? pageable.getSort().toString().replace(":", ".") : "unsorted";
        String cacheKey = CACHE_PREFIX + subId + ":" + pageable.getPageNumber() + ":" + pageable.getPageSize() + ":" + sortKey;

        String cached = redisService.get(cacheKey);
        if (cached != null && !cached.isBlank()) {
            try {
                NotificationPageCache cache = objectMapper.readValue(cached, NotificationPageCache.class);
                logger.debug("Notifications for {} served from cache (page {})", subId, pageable.getPageNumber());
                return cache.toPage(pageable);
            } catch (JsonProcessingException e) {
                logger.warn("Failed to parse cached notifications for {}: {}", subId, e.getMessage());
            }
        }

        Page<NotificationResponse> page = notificationRepository.findByUserSubId(subId, pageable)
                .map(this::toResponse);

        try {
            NotificationPageCache cache = NotificationPageCache.from(page);
            String json = objectMapper.writeValueAsString(cache);
            redisService.setWithTtl(cacheKey, json, cacheTtlSeconds);
            logger.debug("Notifications for {} cached (page {})", subId, pageable.getPageNumber());
        } catch (JsonProcessingException e) {
            logger.warn("Failed to cache notifications for {}: {}", subId, e.getMessage());
        }

        return page;
    }

    public void markAsRead(Long notificationId, String subId) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + notificationId));
        if (!n.getUser().getSub_id().equals(subId)) {
            throw new IllegalArgumentException("Notification does not belong to user");
        }
        n.setRead(true);
        notificationRepository.save(n);
        invalidateCacheForUser(subId);
    }

    private void invalidateCacheForUser(String subId) {
        redisService.deleteKeysByPattern(CACHE_PREFIX + subId + ":*");
        logger.debug("Invalidated notification cache for user {}", subId);
    }

    private NotificationResponse toResponse(Notification n) {
        String createdAtStr = n.getCreatedAt() != null
                ? n.getCreatedAt().atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                : null;
        Long medalId = n.getMedalId();
        String medalName = n.getMedalName();
        String medalDescription = null;
        if (medalName != null && !medalName.isBlank()) {
            try {
                medalDescription = com.MuscleHead.MuscleHead.Medal.MedalName.valueOf(medalName).getDescription();
            } catch (IllegalArgumentException ignored) {
                // enum value may have been removed
            }
        }
        return new NotificationResponse(
                n.getId(),
                n.getType(),
                n.getMessage(),
                createdAtStr,
                n.isRead(),
                medalId,
                medalName,
                medalDescription);
    }
}
