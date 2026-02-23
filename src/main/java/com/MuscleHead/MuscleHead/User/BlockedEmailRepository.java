package com.MuscleHead.MuscleHead.User;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BlockedEmailRepository extends JpaRepository<BlockedEmail, String> {

    Optional<BlockedEmail> findByEmail(String email);
}
