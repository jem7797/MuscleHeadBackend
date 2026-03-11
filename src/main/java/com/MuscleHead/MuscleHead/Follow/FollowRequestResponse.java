package com.MuscleHead.MuscleHead.Follow;

import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FollowRequestResponse {

    private UUID id;
    private UserSummary requester;
    private String followeeSubId;
    private String status;
    private Instant createdAt;
}
