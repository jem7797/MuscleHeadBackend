package com.MuscleHead.MuscleHead.Follow;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.MuscleHead.MuscleHead.User.User;

@Repository
public interface FollowRequestRepository extends JpaRepository<FollowRequest, UUID> {

    Optional<FollowRequest> findById(UUID id);

    @Query("SELECT fr FROM FollowRequest fr WHERE fr.followee = :followee AND fr.status = 'pending' ORDER BY fr.createdAt DESC")
    List<FollowRequest> findPendingByFollowee(@Param("followee") User followee);

    @Query("SELECT fr FROM FollowRequest fr WHERE fr.requester.sub_id = :requesterSubId AND fr.followee.sub_id = :followeeSubId AND fr.status = 'pending'")
    Optional<FollowRequest> findPendingByRequesterAndFollowee(@Param("requesterSubId") String requesterSubId, @Param("followeeSubId") String followeeSubId);
}
