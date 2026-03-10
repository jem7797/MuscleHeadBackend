package com.MuscleHead.MuscleHead.LiveSession;

import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateSessionResponse {

    private UUID id;
    private String hostUserId;
    private String status;
    private Instant createdAt;
}
