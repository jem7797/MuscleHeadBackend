package com.MuscleHead.MuscleHead.LiveSession;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionInviteRepository extends JpaRepository<SessionInvite, UUID> {

    Optional<SessionInvite> findById(UUID id);

    @Query("SELECT i FROM SessionInvite i WHERE i.toUserId = :userId AND i.status = 'pending' ORDER BY i.sentAt DESC")
    List<SessionInvite> findPendingInvitesForUser(@Param("userId") String userId);
}
