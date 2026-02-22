package com.MuscleHead.MuscleHead.Notification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.MuscleHead.MuscleHead.Follow.UserSummary;
import com.MuscleHead.MuscleHead.User.User;
import com.MuscleHead.MuscleHead.User.UserRepository;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    public void createFollowNotification(User recipient, User actor) {
        Notification n = new Notification();
        n.setRecipient(recipient);
        n.setActor(actor);
        n.setType("FOLLOW");
        notificationRepository.save(n);
    }

    public Page<NotificationResponse> getNotificationsForUser(String recipientSubId, Pageable pageable) {
        User recipient = userRepository.findById(recipientSubId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + recipientSubId));
        return notificationRepository.findByRecipientOrderByCreatedAtDesc(recipient, pageable)
                .map(this::toResponse);
    }

    public void markAsRead(Long notificationId, String recipientSubId) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + notificationId));
        if (!n.getRecipient().getSub_id().equals(recipientSubId)) {
            throw new IllegalArgumentException("Notification does not belong to user");
        }
        n.setRead(true);
        notificationRepository.save(n);
    }

    private NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getType(),
                n.getCreatedAt(),
                n.isRead(),
                UserSummary.from(n.getActor()));
    }
}
