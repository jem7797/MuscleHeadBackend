package com.MuscleHead.MuscleHead.Notification;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("SELECT n FROM Notification n WHERE n.user.sub_id = :subId ORDER BY n.createdAt DESC")
    List<Notification> findByUserSubId(@Param("subId") String subId);

    @Query("SELECT n FROM Notification n WHERE n.user.sub_id = :subId ORDER BY n.createdAt DESC")
    Page<Notification> findByUserSubId(@Param("subId") String subId, Pageable pageable);

    @Query("SELECT n FROM Notification n WHERE n.user.sub_id = :subId AND n.isRead = false")
    List<Notification> findUnreadByUserSubId(@Param("subId") String subId);
}
