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

    @Query(value = "SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) ESCAPE '\\' AND (u.privacy_setting IS NULL OR u.privacy_setting != 'hidden')",
           countQuery = "SELECT COUNT(u) FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) ESCAPE '\\' AND (u.privacy_setting IS NULL OR u.privacy_setting != 'hidden')")
    Page<User> findByUsernameContainingIgnoreCaseExcludingHidden(@Param("query") String query, Pageable pageable);

    @Query("SELECT u.sub_id FROM User u WHERE u.sub_id IN :subIds AND (u.privacy_setting IS NULL OR u.privacy_setting != 'hidden')")
    List<String> findVisibleUserSubIds(@Param("subIds") List<String> subIds);

    @Query("SELECT u FROM User u JOIN u.nemesis n WHERE n.sub_id = :nemesisSubId")
    List<User> findUsersWhoHaveAsNemesis(@Param("nemesisSubId") String nemesisSubId);
}
