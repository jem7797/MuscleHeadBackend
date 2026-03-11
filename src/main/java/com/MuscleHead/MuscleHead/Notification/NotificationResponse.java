package com.MuscleHead.MuscleHead.Notification;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private Long id;
    private NotificationType type;
    private String message;
    private String createdAt;
    private boolean read;
    /** Present when type is MEDAL_EARNED */
    private Long medalId;
    /** Present when type is MEDAL_EARNED */
    private String medalName;
    /** Present when type is MEDAL_EARNED */
    private String medalDescription;
    /** Present when type is FOLLOW: the follower's sub/user ID */
    private String actorSubId;
}
