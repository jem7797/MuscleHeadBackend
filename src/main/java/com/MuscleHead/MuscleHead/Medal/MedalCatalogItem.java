package com.MuscleHead.MuscleHead.Medal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedalCatalogItem {

    private MedalName medalName;
    private String description;
    private boolean earned;
    /** ISO 8601 UTC timestamp when earned; null if not earned */
    private String awardedAt;
}
