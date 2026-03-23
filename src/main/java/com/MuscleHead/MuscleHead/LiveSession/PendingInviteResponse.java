package com.MuscleHead.MuscleHead.LiveSession;

import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PendingInviteResponse {

    private UUID inviteId;
    private UUID sessionId;
    private String fromUserId;
    private String message;
    private Instant sentAt;
    private String hostUserName;
    private String status; 
}
