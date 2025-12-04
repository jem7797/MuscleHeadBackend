package com.MuscleHead.MuscleHead.User;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public User createNewUser(User user) {
        logger.debug("Creating new user with sub_id: {}", user != null ? user.getSub_id() : "null");
        if (user == null || user.getSub_id() == null) {
            logger.error("Attempted to create user with null user or sub_id");
            throw new IllegalArgumentException("User and sub_id must not be null");
        }
        if (userRepository.existsById(user.getSub_id())) {
            logger.warn("Attempted to create user that already exists: {}", user.getSub_id());
            throw new IllegalStateException("User already exists: " + user.getSub_id());
        }
        User savedUser = userRepository.save(user);
        logger.info("User created successfully with sub_id: {}", savedUser.getSub_id());
        return savedUser;
    }

    @Transactional
    public Optional<User> updateUser(User updatedUser) {
        logger.debug("Updating user with sub_id: {}", updatedUser != null ? updatedUser.getSub_id() : "null");
        if (updatedUser == null || updatedUser.getSub_id() == null) {
            logger.error("Attempted to update user with null user or sub_id");
            throw new IllegalArgumentException("User and sub_id must not be null");
        }

        return userRepository.findById(updatedUser.getSub_id())
                .map(existingUser -> {
                    logger.debug("Found existing user, updating fields for sub_id: {}", updatedUser.getSub_id());
                    existingUser.setUsername(updatedUser.getUsername());
                    if (updatedUser.getEmail() != null) {
                        existingUser.setEmail(updatedUser.getEmail());
                    }
                    if (updatedUser.getFirst_name() != null) {
                        existingUser.setFirst_name(updatedUser.getFirst_name());
                    }
                    existingUser.setHeight(updatedUser.getHeight());
                    existingUser.setWeight(updatedUser.getWeight());
                    existingUser.setShow_weight(updatedUser.isShow_weight());
                    existingUser.setShow_height(updatedUser.isShow_height());
                    existingUser.setStat_tracking(updatedUser.isStat_tracking());
                    existingUser.setNattyStatus(updatedUser.isNattyStatus());
                    existingUser.setPrivacy_setting(updatedUser.getPrivacy_setting());
                    existingUser.setLifetime_weight_lifted(updatedUser.getLifetime_weight_lifted());
                    existingUser.setLifetime_gym_time(updatedUser.getLifetime_gym_time());
                    existingUser.setNumber_of_followers(updatedUser.getNumber_of_followers());
                    existingUser.setNumber_following(updatedUser.getNumber_following());
                    existingUser.setProfilePicUrl(updatedUser.getProfilePicUrl());
                    existingUser.setXP(updatedUser.getXP());

                    User savedUser = userRepository.save(existingUser);
                    logger.info("User updated successfully with sub_id: {}", savedUser.getSub_id());
                    return savedUser;
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

    @Transactional
    public boolean deleteUserById(String subId) {
        logger.debug("Deleting user with sub_id: {}", subId);
        if (subId == null || subId.isBlank()) {
            logger.warn("Attempted to delete user with null or blank sub_id");
            return false;
        }
        if (!userRepository.existsById(subId)) {
            logger.warn("User not found for deletion: sub_id: {}", subId);
            return false;
        }
        userRepository.deleteById(subId);
        logger.info("User deleted successfully with sub_id: {}", subId);
        return true;
    }

    public Optional<User> getUserByUsername(String username) {
        logger.debug("Getting user by username: {}", username);
        if (username == null || username.isBlank()) {
            logger.error("Attempted to get user with null or blank username");
            throw new IllegalArgumentException("Username must not be blank");
        }
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            logger.debug("Found user with username: {}", username);
        } else {
            logger.debug("User not found with username: {}", username);
        }
        return user;
    }

    public Optional<User> getUserById(String subId) {
        logger.debug("Getting user by sub_id: {}", subId);
        if (subId == null || subId.isBlank()) {
            logger.error("Attempted to get user with null or blank sub_id");
            throw new IllegalArgumentException("User id must not be blank");
        }
        Optional<User> user = userRepository.findById(subId);
        if (user.isPresent()) {
            logger.debug("Found user with sub_id: {}", subId);
        } else {
            logger.debug("User not found with sub_id: {}", subId);
        }
        return user;
    }
}
