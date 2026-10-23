package com.crmapp.backend.identity.repository;

import com.crmapp.backend.identity.model.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, UUID> {

    @Query("select u from User u "
            + "where u.email = :email "
            + "and u.isDeleted = false "
            + "and u.group.isDeleted = false "
            + "and u.group.organization.isDeleted = false")
    Optional<User> findByEmail(@Param("email") String email);

    @Query("select u from User u "
            + "where u.group.id = :groupId "
            + "and u.isDeleted = false "
            + "and u.group.isDeleted = false "
            + "and u.group.organization.isDeleted = false")
    List<User> findByGroup_Id(@Param("groupId") UUID groupId);

}
