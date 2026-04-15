package com.MuscleHead.MuscleHead.LiveSession;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.Instant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionInviteRepository extends JpaRepository<SessionInvite, UUID> {

    Optional<SessionInvite> findById(UUID id);

    Optional<SessionInvite> findByIdAndToUserId(UUID id, String toUserId);

    @Query("""
            SELECT i FROM SessionInvite i
            WHERE i.toUserId = :userId
              AND i.status = 'pending'
              AND i.sentAt >= :cutoff
            ORDER BY i.sentAt DESC
            """)
    List<SessionInvite> findPendingInvitesForUser(
            @Param("userId") String userId,
            @Param("cutoff") java.time.Instant cutoff);

    @Query("""
            SELECT i FROM SessionInvite i
            WHERE i.toUserId = :userId
              AND i.status = 'pending'
              AND i.recipientToastSeenAt IS NULL
              AND i.sentAt >= :cutoff
            ORDER BY i.sentAt DESC
            """)
    List<SessionInvite> findUnseenPendingInvitesForUser(
            @Param("userId") String userId,
            @Param("cutoff") java.time.Instant cutoff);

    long deleteBySentAtBefore(Instant cutoff);
}
