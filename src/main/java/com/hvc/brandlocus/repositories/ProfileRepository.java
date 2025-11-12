package com.hvc.brandlocus.repositories;


import com.hvc.brandlocus.entities.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<Profile, Long>{
}
