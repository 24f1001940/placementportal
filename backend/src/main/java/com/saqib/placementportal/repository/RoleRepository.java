package com.saqib.placementportal.repository;

import com.saqib.placementportal.entity.Role;
import com.saqib.placementportal.entity.RoleName;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName name);
}
