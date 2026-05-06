package com.MuscleHead.MuscleHead.User;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecommendedUserItem {

    private String id;

    private String username;

    @JsonProperty("profile_picture")
    private String profilePicture;

    @JsonProperty("number_of_followers")
    private int numberOfFollowers;

    @JsonProperty("display_name")
    private String displayName;

    static RecommendedUserItem fromUser(User user) {
        if (user == null) {
            return null;
        }
        return new RecommendedUserItem(
                user.getSub_id(),
                user.getUsername(),
                user.getProfilePicUrl(),
                user.getNumber_of_followers(),
                user.getFirst_name());
    }
}
