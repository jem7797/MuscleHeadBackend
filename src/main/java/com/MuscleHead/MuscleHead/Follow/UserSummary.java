package com.MuscleHead.MuscleHead.Follow;

import com.MuscleHead.MuscleHead.User.User;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Light DTO for follower/following lists. Enough for profile navigation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSummary {

    private String subId;
    private String username;
    private String profilePicUrl;
    /** Cache-busting version for profile pic. Append ?v={profilePicVersion} to image URL to bypass CDN/browser cache. */
    private Long profilePicVersion;
    private String gender;

    public static UserSummary from(User user) {
        if (user == null) return null;
        return new UserSummary(
                user.getSub_id(),
                user.getUsername(),
                user.getProfilePicUrl(),
                user.getProfilePicVersion(),
                user.getGender());
    }
}
