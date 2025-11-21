package com.MuscleHead.MuscleHead.User;

// ✅ ADDED: Import for List collection
import java.util.List;

// ✅ ADDED: Import for Workout entity
import com.MuscleHead.MuscleHead.Workout.Workout;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
// ✅ ADDED: Import for OneToMany relationship annotation
import jakarta.persistence.OneToMany;

@Entity
public class User {

    @Id
    private String sub_id;

    private String username;
    private int height;
    private int weight;
    private boolean show_weight;
    private boolean show_height;
    private boolean stat_tracking;
    private String privacy_setting;
    private double lifetime_weight_lifted;
    private double lifetime_gym_time;
    private int number_of_followers;
    private int number_following;
    private String profilePicUrl;

    @Column(updatable = false)
    private int birth_year;

    @Column(updatable = false)
    private String date_created;

    @OneToMany(mappedBy = "user")
    private List<Workout> workouts;

    public User() {
    }

    public String getSub_id() {
        return sub_id;
    }

    public void setSub_id(String sub_id) {
        this.sub_id = sub_id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public boolean isShow_weight() {
        return show_weight;
    }

    public void setShow_weight(boolean show_weight) {
        this.show_weight = show_weight;
    }

    public boolean isShow_height() {
        return show_height;
    }

    public void setShow_height(boolean show_height) {
        this.show_height = show_height;
    }

    public String getProfilePicUrl() {
        return profilePicUrl;
    }

    public void setProfilePicUrl(String profilePicUrl) {
        this.profilePicUrl = profilePicUrl;
    }

    public boolean isStat_tracking() {
        return stat_tracking;
    }

    public void setStat_tracking(boolean stat_tracking) {
        this.stat_tracking = stat_tracking;
    }

    public String getPrivacy_setting() {
        return privacy_setting;
    }

    public void setPrivacy_setting(String privacy_setting) {
        this.privacy_setting = privacy_setting;
    }

    public double getLifetime_weight_lifted() {
        return lifetime_weight_lifted;
    }

    public void setLifetime_weight_lifted(double lifetime_weight_lifted) {
        this.lifetime_weight_lifted = lifetime_weight_lifted;
    }

    public double getLifetime_gym_time() {
        return lifetime_gym_time;
    }

    public void setLifetime_gym_time(double lifetime_gym_time) {
        this.lifetime_gym_time = lifetime_gym_time;
    }

    public int getNumber_of_followers() {
        return number_of_followers;
    }

    public void setNumber_of_followers(int number_of_followers) {
        this.number_of_followers = number_of_followers;
    }

    public int getNumber_following() {
        return number_following;
    }

    public void setNumber_following(int number_following) {
        this.number_following = number_following;
    }

    public int getBirth_year() {
        return birth_year;
    }

    public void setBirth_year(int birth_year) {
        this.birth_year = birth_year;
    }

    public String getDate_created() {
        return date_created;
    }

    public void setDate_created(String date_created) {
        this.date_created = date_created;
    }

    // ✅ ADDED: Getter for workouts collection
    public List<Workout> getWorkouts() {
        return workouts;
    }

    // ✅ ADDED: Setter for workouts collection
    public void setWorkouts(List<Workout> workouts) {
        this.workouts = workouts;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((sub_id == null) ? 0 : sub_id.hashCode());
        result = prime * result + ((username == null) ? 0 : username.hashCode());
        result = prime * result + height;
        result = prime * result + weight;
        result = prime * result + (show_weight ? 1231 : 1237);
        result = prime * result + (show_height ? 1231 : 1237);
        result = prime * result + (stat_tracking ? 1231 : 1237);
        result = prime * result + ((privacy_setting == null) ? 0 : privacy_setting.hashCode());
        long temp;
        temp = Double.doubleToLongBits(lifetime_weight_lifted);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(lifetime_gym_time);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + number_of_followers;
        result = prime * result + number_following;
        result = prime * result + ((profilePicUrl == null) ? 0 : profilePicUrl.hashCode());
        result = prime * result + birth_year;
        result = prime * result + ((date_created == null) ? 0 : date_created.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        User other = (User) obj;
        if (sub_id == null) {
            if (other.sub_id != null)
                return false;
        } else if (!sub_id.equals(other.sub_id))
            return false;
        if (username == null) {
            if (other.username != null)
                return false;
        } else if (!username.equals(other.username))
            return false;
        if (height != other.height)
            return false;
        if (weight != other.weight)
            return false;
        if (show_weight != other.show_weight)
            return false;
        if (show_height != other.show_height)
            return false;
        if (stat_tracking != other.stat_tracking)
            return false;
        if (privacy_setting == null) {
            if (other.privacy_setting != null)
                return false;
        } else if (!privacy_setting.equals(other.privacy_setting))
            return false;
        if (Double.doubleToLongBits(lifetime_weight_lifted) != Double.doubleToLongBits(other.lifetime_weight_lifted))
            return false;
        if (Double.doubleToLongBits(lifetime_gym_time) != Double.doubleToLongBits(other.lifetime_gym_time))
            return false;
        if (number_of_followers != other.number_of_followers)
            return false;
        if (number_following != other.number_following)
            return false;
        if (profilePicUrl == null) {
            if (other.profilePicUrl != null)
                return false;
        } else if (!profilePicUrl.equals(other.profilePicUrl))
            return false;
        if (birth_year != other.birth_year)
            return false;
        if (date_created == null) {
            if (other.date_created != null)
                return false;
        } else if (!date_created.equals(other.date_created))
            return false;
        return true;
    }

}