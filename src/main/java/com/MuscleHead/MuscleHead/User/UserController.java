package com.MuscleHead.MuscleHead.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("user/api/")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
        logger.info("Creating new user with sub_id: {}", user.getSub_id());
        try {
            User createdUser = userService.createNewUser(user);
            logger.info("Successfully created user with sub_id: {}", createdUser.getSub_id());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (IllegalStateException ex) {
            logger.warn("Failed to create user - user already exists: {}", user.getSub_id());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception ex) {
            logger.error("Error creating user with sub_id: {}", user.getSub_id(), ex);
            throw ex;
        }
    }

    @PutMapping("/{subId}")
    public ResponseEntity<User> updateUser(
            @PathVariable String subId,
            @Valid @RequestBody User user) {
        logger.info("Updating user with sub_id: {}", subId);
        user.setSub_id(subId);
        return userService.updateUser(user)
                .map(updatedUser -> {
                    logger.info("Successfully updated user with sub_id: {}", subId);
                    return ResponseEntity.ok(updatedUser);
                })
                .orElseGet(() -> {
                    logger.warn("User not found for update: sub_id: {}", subId);
                    return ResponseEntity.notFound().build();
                });
    }

    @DeleteMapping("/{subId}")
    public ResponseEntity<Void> deleteUser(@PathVariable String subId) {
        logger.info("Deleting user with sub_id: {}", subId);
        if (userService.deleteUserById(subId)) {
            logger.info("Successfully deleted user with sub_id: {}", subId);
            return ResponseEntity.noContent().build();
        }
        logger.warn("User not found for deletion: sub_id: {}", subId);
        return ResponseEntity.notFound().build();
    }

    @GetMapping
    public ResponseEntity<User> getUser(@RequestParam(required = false) String username,
            @RequestParam(required = false) String subId) {
        if (subId != null && !subId.isBlank()) {
            logger.debug("Getting user by sub_id: {}", subId);
            return userService.getUserById(subId)
                    .map(user -> {
                        logger.debug("Found user with sub_id: {}", subId);
                        return ResponseEntity.ok(user);
                    })
                    .orElseGet(() -> {
                        logger.warn("User not found with sub_id: {}", subId);
                        return ResponseEntity.notFound().build();
                    });
        }
        if (username != null && !username.isBlank()) {
            logger.debug("Getting user by username: {}", username);
            return userService.getUserByUsername(username)
                    .map(user -> {
                        logger.debug("Found user with username: {}", username);
                        return ResponseEntity.ok(user);
                    })
                    .orElseGet(() -> {
                        logger.warn("User not found with username: {}", username);
                        return ResponseEntity.notFound().build();
                    });
        }
        logger.warn("Get user request missing both username and subId parameters");
        return ResponseEntity.badRequest().build();
    }
}
