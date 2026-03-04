package com.MuscleHead.MuscleHead.User;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByUsername(String username);

    Page<User> findByUsernameContainingIgnoreCase(String query, Pageable pageable);

    @Query("SELECT u FROM User u JOIN u.nemesis n WHERE n.sub_id = :nemesisSubId")
    List<User> findUsersWhoHaveAsNemesis(@Param("nemesisSubId") String nemesisSubId);
}
