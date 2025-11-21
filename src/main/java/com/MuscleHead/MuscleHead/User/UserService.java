package com.MuscleHead.MuscleHead.User;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public User createNewUser(User user) {
        if (user == null || user.getSub_id() == null) {
            throw new IllegalArgumentException("User and sub_id must not be null");
        }
        if (userRepository.existsById(user.getSub_id())) {
            throw new IllegalStateException("User already exists: " + user.getSub_id());
        }
        return userRepository.save(user);
    }

    @Transactional
    public Optional<User> updateUser(User updatedUser) {
        if (updatedUser == null || updatedUser.getSub_id() == null) {
            throw new IllegalArgumentException("User and sub_id must not be null");
        }

        return userRepository.findById(updatedUser.getSub_id())
                .map(existingUser -> {
                    existingUser.setUsername(updatedUser.getUsername());
                    existingUser.setHeight(updatedUser.getHeight());
                    existingUser.setWeight(updatedUser.getWeight());
                    existingUser.setShow_weight(updatedUser.isShow_weight());
                    existingUser.setShow_height(updatedUser.isShow_height());
                    existingUser.setStat_tracking(updatedUser.isStat_tracking());
                    existingUser.setPrivacy_setting(updatedUser.getPrivacy_setting());
                    existingUser.setLifetime_weight_lifted(updatedUser.getLifetime_weight_lifted());
                    existingUser.setLifetime_gym_time(updatedUser.getLifetime_gym_time());
                    existingUser.setNumber_of_followers(updatedUser.getNumber_of_followers());
                    existingUser.setNumber_following(updatedUser.getNumber_following());
                    existingUser.setProfilePicUrl(updatedUser.getProfilePicUrl());
                    existingUser.setXP(updatedUser.getXP());

                    return userRepository.save(existingUser);
                });
    }

    @Transactional
    public boolean deleteUser(User user) {
        if (user == null || user.getSub_id() == null) {
            return false;
        }
        if (!userRepository.existsById(user.getSub_id())) {
            return false;
        }
        userRepository.deleteById(user.getSub_id());
        return true;
    }

    public Optional<User> getUserByUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username must not be blank");
        }
        return userRepository.findByUsername(username);
    }

    public Optional<User> getUserById(String subId) {
        if (subId == null || subId.isBlank()) {
            throw new IllegalArgumentException("User id must not be blank");
        }
        return userRepository.findById(subId);
    }
}
