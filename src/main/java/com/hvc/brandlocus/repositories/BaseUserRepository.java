package com.hvc.brandlocus.repositories;


import com.hvc.brandlocus.entities.BaseUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface BaseUserRepository extends JpaRepository<BaseUser, Long>, JpaSpecificationExecutor<BaseUser> {

    Optional<BaseUser> findByEmail(String email);

    boolean existsByEmail(String email);
}
