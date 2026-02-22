package com.MuscleHead.MuscleHead.Notification;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.MuscleHead.MuscleHead.User.User;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByRecipientOrderByCreatedAtDesc(User recipient, Pageable pageable);
}
