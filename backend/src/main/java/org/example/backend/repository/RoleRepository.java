package org.example.backend.repository;

import org.example.backend.domain.entity.Role;
import org.example.backend.domain.enums.RoleName;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName name);
}
