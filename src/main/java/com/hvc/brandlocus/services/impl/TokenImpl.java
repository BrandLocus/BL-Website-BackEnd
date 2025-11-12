package com.hvc.brandlocus.services.impl;

import com.hvc.brandlocus.dto.request.EmailOtpRequest;
import com.hvc.brandlocus.dto.request.TokenRequest;
import com.hvc.brandlocus.dto.request.VerifyTokenRequest;
import com.hvc.brandlocus.entities.Token;
import com.hvc.brandlocus.repositories.TokenRepository;
import com.hvc.brandlocus.services.TokenService;
import com.hvc.brandlocus.utils.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

import static com.hvc.brandlocus.utils.ResponseUtils.createFailureResponse;
import static com.hvc.brandlocus.utils.ResponseUtils.createSuccessResponse;


@Service
@RequiredArgsConstructor
@Slf4j
public class TokenImpl implements TokenService {

    private final TokenRepository tokenRepository;

    @Override
    public ResponseEntity<ApiResponse<?>> getToken(TokenRequest request) {
       try {
           String email = request.getEmail();
           if (email == null || email.isEmpty()) {
               return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                       .body(createFailureResponse("", "Email is required"));
           }

           Optional<Token> existing = tokenRepository.findByEmail(email);
           Token token;

           if (existing.isEmpty()) {
               token = new Token();
               token.setEmail(email);
           } else {
               token = existing.get();

               if (token.getNextRequestAllowedAt() != null &&
                       token.getNextRequestAllowedAt().isAfter(LocalDateTime.now())) {
                   long secondsLeft = java.time.Duration.between(LocalDateTime.now(), token.getNextRequestAllowedAt()).getSeconds();
                   return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                           .body(createFailureResponse("", "Please wait " + secondsLeft + "s before requesting a new OTP"));
               }

               token.setToken(generateOtp());
               token.setExpiry(LocalDateTime.now().plusMinutes(5));
               token.setNextRequestAllowedAt(LocalDateTime.now().plusSeconds(60));
               token.setValidated(false);
               token.setExpired(false);
               token.setUsed(false);
           }
           tokenRepository.save(token);

           log.info("build email request");

           EmailOtpRequest tokenRequest = new EmailOtpRequest (
                   email,
                   "Email Verification",
                   "Your verification code is: " + token.getToken(),
                   false
           );

           log.info("call notification service to send email to {}",email);

           log.info("This is the token: {}", token.getToken());

           return ResponseEntity.ok(createSuccessResponse("", "OTP sent successfully"));

       }catch (Exception e) {
           return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                   .body(createFailureResponse(e.getLocalizedMessage(), "An unexpected error occurred while sending token"));
       }
    }

    @Override
    public ResponseEntity<ApiResponse<?>> verifyToken(VerifyTokenRequest request) {
        try {
            String otp = request.getToken();
            String email = request.getEmail();

            if ((email == null || email.isEmpty())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createFailureResponse("", "Email is required"));
            }

            Optional<Token> existingToken;

                existingToken = tokenRepository.findByEmail(email);

            if (existingToken.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createFailureResponse("", "No token record found"));
            }

            Token token = existingToken.get();

            if (!token.getToken().equals(otp)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createFailureResponse("", "Invalid OTP"));
            }

            if (token.getExpiry().isBefore(LocalDateTime.now())) {
                token.setExpired(true);
                tokenRepository.save(token);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createFailureResponse("", "OTP has expired"));
            }

            if (token.isUsed()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(createFailureResponse("", "OTP has already been used"));
            }

            token.setValidated(true);
            token.setUsed(true);
            tokenRepository.save(token);

            return ResponseEntity.ok(createSuccessResponse("", "OTP verified successfully"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createFailureResponse(e.getLocalizedMessage(), "An unexpected error occurred while verifying token"));
        }
    }

    private String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // generates 6-digit OTP
        return String.valueOf(otp);
    }
}
