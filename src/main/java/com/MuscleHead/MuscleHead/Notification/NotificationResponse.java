package com.MuscleHead.MuscleHead.Notification;

import com.MuscleHead.MuscleHead.Follow.UserSummary;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private Long id;
    private String type;
    private String createdAt;
    private boolean read;
    private UserSummary actor;
}
