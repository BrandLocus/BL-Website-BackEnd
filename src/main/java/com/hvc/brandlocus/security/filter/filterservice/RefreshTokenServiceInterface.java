package com.hvc.brandlocus.security.filter.filterservice;

import com.hvc.brandlocus.entities.BaseUser;
import com.hvc.brandlocus.entities.RefreshToken;

public interface RefreshTokenServiceInterface {
    RefreshToken createRefreshToken(BaseUser user);

    /**
     * Validate if the refresh token is still valid (not expired or revoked)
     */
    boolean isValid(RefreshToken token);

    /**
     * Revoke a single refresh token
     */
    void revokeToken(RefreshToken token);

    /**
     * Revoke all refresh tokens for a user
     */
    void revokeAllTokensForUser(BaseUser user);

    /**
     * Retrieve a refresh token by its string value
     */
    RefreshToken getByToken(String token);
}
