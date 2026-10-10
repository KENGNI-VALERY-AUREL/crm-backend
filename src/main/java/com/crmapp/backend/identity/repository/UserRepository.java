package com.crmapp.backend.identity.repository;

import com.crmapp.backend.identity.model.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    List<User> findByGroup_Id(UUID groupId);

}
