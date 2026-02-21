package com.MuscleHead.MuscleHead.User;

import com.MuscleHead.MuscleHead.Rank.RankRepository;

import java.util.HashMap;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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

    @Autowired
    private RankRepository rankRepository;

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
        // Assign default rank (Newbie, level 0) if not provided
        if (user.getRank() == null) {
            rankRepository.findByLevel(0)
                    .ifPresentOrElse(
                            user::setRank,
                            () -> logger.warn("Cannot assign default rank to new user: rank with level 0 (Newbie) not found. Ensure RankSeeder has run.")
                    );
        }
        User savedUser = userRepository.save(user);
        logger.info("User created successfully with sub_id: {}", savedUser.getSub_id());
        return savedUser;
    }

    /**
     * Partial update: merges only non-null fields from the request into the existing user.
     * Use for PATCH requests where the frontend sends only changed fields.
     */
    @Transactional
    public Optional<User> partialUpdate(String subId, UpdateUserRequest request) {
        if (subId == null || subId.isBlank()) {
            throw new IllegalArgumentException("sub_id must not be blank");
        }
        if (request == null) {
            throw new IllegalArgumentException("Update request must not be null");
        }

        return userRepository.findById(subId)
                .map(existingUser -> {
                    ensureUserHasRank(existingUser);

                    if (request.getUsername() != null) {
                        if (request.getUsername().isBlank()) {
                            throw new IllegalArgumentException("Username cannot be empty");
                        }
                        existingUser.setUsername(request.getUsername());
                    }
                    if (request.getEmail() != null) {
                        if (request.getEmail().isBlank()) {
                            throw new IllegalArgumentException("Email cannot be empty");
                        }
                        existingUser.setEmail(request.getEmail());
                    }
                    if (request.getFirst_name() != null) {
                        if (request.getFirst_name().isBlank()) {
                            throw new IllegalArgumentException("First name cannot be empty");
                        }
                        existingUser.setFirst_name(request.getFirst_name());
                    }
                    if (request.getHeight() != null) {
                        if (request.getHeight() < 0) {
                            throw new IllegalArgumentException("Height must be 0 or greater");
                        }
                        existingUser.setHeight(request.getHeight());
                    }
                    if (request.getWeight() != null) {
                        if (request.getWeight() < 0) {
                            throw new IllegalArgumentException("Weight must be 0 or greater");
                        }
                        existingUser.setWeight(request.getWeight());
                    }
                    if (request.getShow_weight() != null) {
                        existingUser.setShow_weight(request.getShow_weight());
                    }
                    if (request.getShow_height() != null) {
                        existingUser.setShow_height(request.getShow_height());
                    }
                    if (request.getStat_tracking() != null) {
                        existingUser.setStat_tracking(request.getStat_tracking());
                    }
                    if (request.getPrivacy_setting() != null) {
                        existingUser.setPrivacy_setting(request.getPrivacy_setting());
                    }
                    if (request.getProfilePicUrl() != null) {
                        existingUser.setProfilePicUrl(request.getProfilePicUrl());
                    }
                    if (request.getNattyStatus() != null) {
                        existingUser.setNattyStatus(request.getNattyStatus());
                    }
                    if (request.getBio() != null) {
                        existingUser.setBio(request.getBio());
                    }
                    if (request.getWorkoutSchedule() != null) {
                        existingUser.setWorkoutSchedule(new HashMap<>(request.getWorkoutSchedule()));
                    }

                    User savedUser = userRepository.save(existingUser);
                    logger.info("User partially updated successfully with sub_id: {}", savedUser.getSub_id());
                    return savedUser;
                });
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
                    ensureUserHasRank(existingUser);
                    if (updatedUser.getEmail() != null) {
                        existingUser.setEmail(updatedUser.getEmail());
                    }
                    if (updatedUser.getFirst_name() != null) {
                        existingUser.setFirst_name(updatedUser.getFirst_name());
                    }
                    existingUser.setUsername(updatedUser.getUsername());
                    if (updatedUser.getHeight() != null) {
                        existingUser.setHeight(updatedUser.getHeight());
                    }
                    if (updatedUser.getWeight() != null) {
                        existingUser.setWeight(updatedUser.getWeight());
                    }
                    existingUser.setShow_weight(updatedUser.isShow_weight());
                    existingUser.setShow_height(updatedUser.isShow_height());
                    existingUser.setStat_tracking(updatedUser.isStat_tracking());
                    existingUser.setNattyStatus(updatedUser.isNattyStatus());
                    existingUser.setPrivacy_setting(updatedUser.getPrivacy_setting());
                    existingUser.setLifetime_weight_lifted(updatedUser.getLifetime_weight_lifted());
                    existingUser.setHighest_weight_lifted(updatedUser.getHighest_weight_lifted());
                    existingUser.setLifetime_gym_time(updatedUser.getLifetime_gym_time());
                    existingUser.setNumber_of_followers(updatedUser.getNumber_of_followers());
                    existingUser.setNumber_following(updatedUser.getNumber_following());
                    existingUser.setProfilePicUrl(updatedUser.getProfilePicUrl());
                    existingUser.setBio(updatedUser.getBio());
                    existingUser.setXP(updatedUser.getXP());
                    if (updatedUser.getWorkoutSchedule() != null) {
                        existingUser.setWorkoutSchedule(new HashMap<>(updatedUser.getWorkoutSchedule()));
                    }

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

    @Transactional
    public Optional<User> getUserByUsername(String username) {
        logger.debug("Getting user by username: {}", username);
        if (username == null || username.isBlank()) {
            logger.error("Attempted to get user with null or blank username");
            throw new IllegalArgumentException("Username must not be blank");
        }
        return userRepository.findByUsername(username)
                .map(this::ensureUserHasRank);
    }

    /**
     * Search users by username (partial, case-insensitive).
     * Requires at least 2 characters. Returns paginated results.
     */
    @Transactional
    public Page<User> searchUsers(String query, Pageable pageable) {
        if (query == null || query.trim().length() < 2) {
            throw new IllegalArgumentException("Search query must be at least 2 characters");
        }
        Page<User> page = userRepository.findByUsernameContainingIgnoreCase(query.trim(), pageable);
        page.getContent().forEach(this::ensureUserHasRank);
        return page;
    }

    @Transactional
    public Optional<User> getUserById(String subId) {
        logger.debug("Getting user by sub_id: {}", subId);
        if (subId == null || subId.isBlank()) {
            logger.error("Attempted to get user with null or blank sub_id");
            throw new IllegalArgumentException("User id must not be blank");
        }
        return userRepository.findById(subId)
                .map(this::ensureUserHasRank);
    }

    /**
     * Assigns default rank (Newbie, level 0) if user has none. Fixes existing users
     * who had null rank. Returns the user (updated if rank was assigned).
     */
    private User ensureUserHasRank(User user) {
        if (user.getRank() == null) {
            rankRepository.findByLevel(0)
                    .ifPresentOrElse(
                            rank -> {
                                user.setRank(rank);
                                userRepository.save(user);
                                logger.debug("Assigned default rank to user: {}", user.getSub_id());
                            },
                            () -> logger.warn("Cannot assign default rank: rank with level 0 (Newbie) not found. Ensure RankSeeder has run.")
                    );
        }
        return user;
    }

    /**
     * If the user's XP is divisible by 5, sets the user's rank to the corresponding
     * rank (level = XP/5, capped at 19) and saves the user.
     */
    @Transactional
    public void levelUp(User user) {
        if (user == null || user.getSub_id() == null) {
            return;
        }
        // Ensure user has a rank (e.g. existing users with 0-4 XP who never got one)
        if (user.getRank() == null) {
            rankRepository.findByLevel(0)
                    .ifPresent(rank -> {
                        user.setRank(rank);
                        userRepository.save(user);
                        logger.info("User {} assigned default rank (Newbie)", user.getSub_id());
                    });
            return;
        }
        int xp = user.getXP();
        if (xp < 0 || xp % 5 != 0) {
            return;
        }
        int rankLevel = Math.min(xp / 5, 19);
        rankRepository.findByLevel(rankLevel)
                .ifPresent(rank -> {
                    user.setRank(rank);
                    userRepository.save(user);
                    logger.info("User {} leveled up to rank level {} ({})", user.getSub_id(), rankLevel,
                            rank.getName());
                });
    }
}