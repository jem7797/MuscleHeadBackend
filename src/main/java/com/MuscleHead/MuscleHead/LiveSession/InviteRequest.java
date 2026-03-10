package com.MuscleHead.MuscleHead.LiveSession;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class InviteRequest {

    @NotBlank(message = "toUserId is required")
    private String toUserId;

    private String message;
}
