package com.MuscleHead.MuscleHead.User;

import com.MuscleHead.MuscleHead.Notification.NotificationService;
import com.MuscleHead.MuscleHead.Notification.NotificationType;
import com.MuscleHead.MuscleHead.Rank.RankRepository;
import com.MuscleHead.MuscleHead.cache.RedisService;
import com.MuscleHead.MuscleHead.exception.UnderAgeException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private static final String USER_CACHE_PREFIX = "user:";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RankRepository rankRepository;

    @Autowired
    private BlockedEmailRepository blockedEmailRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${user.cache.ttl-seconds:3600}")
    private int userCacheTtlSeconds;

    private static final int MINIMUM_AGE = 13;

    /**
     * Records a minor signup attempt by adding the email to the blocked list.
     * Called when someone under 13 attempts to sign up.
     */
    @Transactional
    public void recordMinorSignupAttempt(MinorSignupAttemptRequest request) {
        BlockedEmail blocked = new BlockedEmail();
        blocked.setEmail(request.getEmail().trim().toLowerCase());
        blocked.setBirth_date(request.getBirth_date());
        blockedEmailRepository.save(blocked);
        logger.warn("Recorded minor signup attempt: email={}, username={}", blocked.getEmail(), request.getUsername());
    }

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

        LocalDate birthDate = user.getBirth_date();
        if (birthDate == null && user.getBirth_year() != null) {
            birthDate = LocalDate.of(user.getBirth_year(), 1, 1);
        }
        int age = birthDate != null
                ? java.time.Period.between(birthDate, LocalDate.now()).getYears()
                : (user.getBirth_year() != null ? LocalDate.now().getYear() - user.getBirth_year() : -1);

        if (age >= 0 && age < MINIMUM_AGE) {
            BlockedEmail blocked = new BlockedEmail();
            blocked.setEmail(user.getEmail().trim().toLowerCase());
            blocked.setBirth_date(birthDate != null ? birthDate : LocalDate.of(user.getBirth_year(), 1, 1));
            blockedEmailRepository.save(blocked);
            logger.warn("Blocked signup: under 13, email: {}", blocked.getEmail());
            throw new UnderAgeException("You must be 13 or older to sign up.");
        }

        var existingBlock = blockedEmailRepository.findByEmail(user.getEmail().trim().toLowerCase());
        if (existingBlock.isPresent()) {
            BlockedEmail block = existingBlock.get();
            if (!block.hasTurned13()) {
                logger.warn("Blocked signup: email still blocked: {}", block.getEmail());
                throw new UnderAgeException("This email cannot be used for signup until you are 13 or older.");
            }
            if (birthDate != null && birthDate.equals(block.getBirth_date())) {
                blockedEmailRepository.delete(block);
                logger.info("Unblocked email after user turned 13: {}", block.getEmail());
            } else {
                throw new UnderAgeException("This email cannot be used for signup until you are 13 or older.");
            }
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
                        existingUser.setProfilePicVersion(System.currentTimeMillis());
                    }
                    if (request.getNattyStatus() != null) {
                        existingUser.setNattyStatus(request.getNattyStatus());
                    }
                    if (request.getBio() != null) {
                        existingUser.setBio(request.getBio());
                    }
                    if (request.getGender() != null) {
                        existingUser.setGender(request.getGender());
                    }
                    if (request.getWorkoutSchedule() != null) {
                        existingUser.setWorkoutSchedule(new HashMap<>(request.getWorkoutSchedule()));
                    }
                    if (request.getNemesisSubIds() != null) {
                        List<User> nemesisUsers = new ArrayList<>();
                        for (String nemesisSubId : request.getNemesisSubIds()) {
                            if (nemesisSubId != null && !nemesisSubId.equals(subId)) {
                                userRepository.findById(nemesisSubId).ifPresent(nemesisUsers::add);
                            }
                        }
                        existingUser.setNemesis(nemesisUsers);
                    }

                    User savedUser = userRepository.save(existingUser);
                    invalidateUserCache(savedUser.getSub_id());
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
                    if (updatedUser.getProfilePicUrl() != null) {
                        existingUser.setProfilePicUrl(updatedUser.getProfilePicUrl());
                        existingUser.setProfilePicVersion(System.currentTimeMillis());
                    }
                    existingUser.setBio(updatedUser.getBio());
                    existingUser.setGender(updatedUser.getGender());
                    existingUser.setXP(updatedUser.getXP());
                    if (updatedUser.getWorkoutSchedule() != null) {
                        existingUser.setWorkoutSchedule(new HashMap<>(updatedUser.getWorkoutSchedule()));
                    }

                    User savedUser = userRepository.save(existingUser);
                    invalidateUserCache(savedUser.getSub_id());
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
        invalidateUserCache(subId);
        logger.info("User deleted successfully with sub_id: {}", subId);
        return true;
    }

    /**
     * Removes a specific nemesis from a user's nemesis list.
     * @return true if the nemesis was found and removed, false otherwise
     */
    @Transactional
    public boolean removeNemesis(String userSubId, String nemesisSubId) {
        if (userSubId == null || nemesisSubId == null || userSubId.equals(nemesisSubId)) {
            return false;
        }
        return userRepository.findById(userSubId)
                .map(user -> {
                    if (user.getNemesis() == null) return false;
                    boolean removed = user.getNemesis().removeIf(n -> nemesisSubId.equals(n.getSub_id()));
                    if (removed) {
                        userRepository.save(user);
                        invalidateUserCache(userSubId);
                        logger.info("User {} removed nemesis {}", userSubId, nemesisSubId);
                    }
                    return removed;
                })
                .orElse(false);
    }

    @Transactional
    public Optional<User> getUserByUsername(String username) {
        logger.debug("Getting user by username: {}", username);
        if (username == null || username.isBlank()) {
            logger.error("Attempted to get user with null or blank username");
            throw new IllegalArgumentException("Username must not be blank");
        }
        return userRepository.findByUsername(username)
                .map(user -> {
                    User ensured = ensureUserHasRank(user);
                    cacheUser(ensured);
                    return ensured;
                });
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
        Page<User> page = userRepository.findByUsernameContainingIgnoreCaseExcludingHidden(query.trim(), pageable);
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

        String cacheKey = USER_CACHE_PREFIX + subId;
        String cached = redisService.get(cacheKey);
        if (cached != null && !cached.isBlank()) {
            try {
                User user = objectMapper.readValue(cached, User.class);
                logger.debug("User {} served from cache", subId);
                return Optional.of(user);
            } catch (JsonProcessingException e) {
                logger.warn("Failed to parse cached user {}: {}", subId, e.getMessage());
            }
        }

        return userRepository.findById(subId)
                .map(user -> {
                    User ensured = ensureUserHasRank(user);
                    cacheUser(ensured);
                    return ensured;
                });
    }

    private void cacheUser(User user) {
        if (user == null || user.getSub_id() == null) return;
        try {
            String json = objectMapper.writeValueAsString(user);
            redisService.setWithTtl(USER_CACHE_PREFIX + user.getSub_id(), json, userCacheTtlSeconds);
            logger.debug("User {} cached", user.getSub_id());
        } catch (JsonProcessingException e) {
            logger.warn("Failed to cache user {}: {}", user.getSub_id(), e.getMessage());
        }
    }

    private void invalidateUserCache(String subId) {
        if (subId == null || subId.isBlank()) return;
        redisService.delete(USER_CACHE_PREFIX + subId);
        logger.debug("Invalidated user cache for {}", subId);
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
                    boolean actuallyLeveledUp = user.getRank() == null || user.getRank().getLevel() != rankLevel;
                    user.setRank(rank);
                    userRepository.save(user);
                    invalidateUserCache(user.getSub_id());
                    logger.info("User {} leveled up to rank level {} ({})", user.getSub_id(), rankLevel,
                            rank.getName());
                    if (actuallyLeveledUp && rankLevel > 0) {
                        String message = "You leveled up to " + rank.getName() + "!";
                        notificationService.createNotification(user, NotificationType.LEVEL_UP, message);
                    }
                });
    }
}