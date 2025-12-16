package com.hvc.brandlocus.repositories;

import com.hvc.brandlocus.entities.BaseUser;
import com.hvc.brandlocus.entities.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByUser(BaseUser user);
    List<RefreshToken> findAllByUserAndRevokedFalse(BaseUser user);
    Optional<RefreshToken> findFirstByUserOrderByIdDesc(BaseUser user);


}
