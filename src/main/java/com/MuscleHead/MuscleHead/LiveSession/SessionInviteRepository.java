package com.MuscleHead.MuscleHead.LiveSession;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import java.time.Instant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface SessionInviteRepository extends JpaRepository<SessionInvite, UUID> {

    Optional<SessionInvite> findByIdAndToUserId(UUID id, String toUserId);

    boolean existsByIdAndToUserId(UUID id, String toUserId);

    @Query("""
            SELECT i FROM SessionInvite i JOIN FETCH i.session
            WHERE i.toUserId = :userId
              AND i.status = 'pending'
              AND i.sentAt >= :cutoff
            ORDER BY i.sentAt DESC
            """)
    List<SessionInvite> findPendingInvitesForUser(
            @Param("userId") String userId,
            @Param("cutoff") Instant cutoff);

    @Query("""
            SELECT i FROM SessionInvite i JOIN FETCH i.session
            WHERE i.toUserId = :userId
              AND i.status = 'pending'
              AND i.recipientToastSeenAt IS NULL
              AND i.sentAt >= :cutoff
            ORDER BY i.sentAt DESC
            """)
    List<SessionInvite> findUnseenPendingInvitesForUser(
            @Param("userId") String userId,
            @Param("cutoff") Instant cutoff);

    @Transactional
    @Modifying
    @Query("""
            UPDATE SessionInvite i
            SET i.recipientToastSeenAt = :seenAt
            WHERE i.id = :inviteId
              AND i.toUserId = :userId
              AND i.recipientToastSeenAt IS NULL
            """)
    int markRecipientToastSeenIfNull(
            @Param("inviteId") UUID inviteId,
            @Param("userId") String userId,
            @Param("seenAt") Instant seenAt);

    /**
     * PostgreSQL: delete at most {@code batchSize} expired rows per call (reduces long locks).
     */
    @Transactional
    @Modifying
    @Query(value = """
            DELETE FROM session_invites
            WHERE id IN (
                SELECT id FROM session_invites
                WHERE sent_at < :cutoff
                LIMIT :batchSize
            )
            """, nativeQuery = true)
    int deleteExpiredInvitesBatch(@Param("cutoff") Instant cutoff, @Param("batchSize") int batchSize);
}
