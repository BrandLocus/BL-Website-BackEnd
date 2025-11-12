package com.hvc.brandlocus.repositories;


import com.hvc.brandlocus.entities.Roles;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RolesRepository extends JpaRepository<Roles, Long> {
    boolean existsByName(String name);
    Optional<Roles> findByName(String name);
}
