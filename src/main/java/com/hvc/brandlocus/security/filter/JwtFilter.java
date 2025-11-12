package com.hvc.brandlocus.security.filter;


import com.hvc.brandlocus.security.filter.filterservice.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            final String requestURI = request.getRequestURI();
            log.info("This is the uri: {}", requestURI);
            log.info("Incoming Request URI: {}", requestURI);
            log.info("Full Request URL: {}", request.getRequestURL());

            if (isExemptedUrl(requestURI)) {
                filterChain.doFilter(request, response);
                return;
            }

            final String authHeader = request.getHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            final String jwt = authHeader.substring(7);
            log.info("Extracted JWT: {}", jwt); // Debug token


            final String userEmail = jwtService.extractUsername(jwt);
            log.info("user email: {}", userEmail);

            if (userEmail.isEmpty() || SecurityContextHolder.getContext().getAuthentication() != null) {
                log.info("Skipped authentication - userEmail empty or already authenticated");
                filterChain.doFilter(request, response);
                return;
            }

            log.info("load user: {}", userEmail);
            UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

            if (jwtService.isTokenValid(jwt, userDetails)) {
                SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
                UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );
                token.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                securityContext.setAuthentication(token);
                SecurityContextHolder.setContext(securityContext);
                log.info("User {} authenticated successfully", userEmail);
            } else {
                log.info("Invalid JWT for user {}", userEmail);
                sendErrorResponse(response, "Invalid or expired token");
                return;
            }

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            log.error("Authentication error for request {}: {}", request.getRequestURI(), e.getMessage());
            sendErrorResponse(response, e.getMessage());
        }
    }

    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        String errorMessage = "{\"error\": \"Request failed\", \"message\": \"" + message + "\"}";
        response.getWriter().write(errorMessage);
    }

    private boolean isExemptedUrl(String requestURI) {
        boolean isExempted = requestURI != null && (
                requestURI.startsWith("/api/v1/auth/") ||
                        requestURI.startsWith("/token/") ||
                        requestURI.startsWith("/actuator/") ||
                        requestURI.equals("/swagger-ui.html") ||
                        requestURI.equals("/swagger-ui/index.html") ||
                        requestURI.startsWith("/swagger-ui/") ||
                        requestURI.startsWith("/v3/api-docs") ||
                        requestURI.startsWith("/v3/api-docs/") ||
                        requestURI.equals("/api-docs/swagger-config") ||
                        requestURI.startsWith("/swagger-resources/") ||
                        requestURI.startsWith("/webjars/")

        );

        log.info("Checking URL exemption - URI: {}, Exempted: {}", requestURI, isExempted);
        return isExempted;
    }


}




