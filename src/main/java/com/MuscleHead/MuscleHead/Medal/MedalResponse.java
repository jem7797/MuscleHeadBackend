package com.MuscleHead.MuscleHead.Medal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedalResponse {

    private Long id;
    private MedalName medalName;
    private String description;
    private String awardedAt;
}
