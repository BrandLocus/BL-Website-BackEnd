package com.hvc.brandlocus.security.filter.filterservice;


import com.hvc.brandlocus.entities.BaseUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;


@Service
@RequiredArgsConstructor
@Slf4j
public class JwtServiceImpl implements  JwtService{


     SecretKey key = Jwts.SIG.HS256.key().build();
    String SECRET_KEY = Encoders.BASE64.encode(key.getEncoded());



    @Override
    public String generateToken(UserDetails userDetails) {
        BaseUser user = (BaseUser) userDetails;
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("Active", user.isEnabled())
                .claim("Email", user.getEmail())
                .claim("role",user.getRole().getName())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 36000000))
                .signWith(getSigningKey())
                .compact();
    }
    @Override
    public String extractUsername(String token){
        return extractClaim(token, Claims::getSubject);
    }


    @Override
    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        log.info("registered email: {}", username);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims,T> resolver){
        Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    private Claims extractAllClaims(String token){
        return  Jwts
                .parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }


}
