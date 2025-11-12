package com.hvc.brandlocus.seeder;

import com.hvc.brandlocus.entities.BaseUser;
import com.hvc.brandlocus.entities.Roles;
import com.hvc.brandlocus.enums.UserRoles;
import com.hvc.brandlocus.enums.UserStatus;
import com.hvc.brandlocus.repositories.BaseUserRepository;
import com.hvc.brandlocus.repositories.RolesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminSeeder implements CommandLineRunner {

    private final BaseUserRepository baseUserRepository;
    private final RolesRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedAdminUser();
    }

    private void seedAdminUser() {
        String adminEmail = "admin@brandlocus.com";

        Optional<BaseUser> existingAdmin = baseUserRepository.findByEmail(adminEmail);
        if (existingAdmin.isPresent()) {
            log.info("✅ Admin user already exists: {}", adminEmail);
            return;
        }
        Roles adminRole = roleRepository.findByName(UserRoles.ADMIN.getValue())
                .orElseGet(() -> {
                    Roles role = new Roles();
                    role.setName(UserRoles.ADMIN.getValue());
                    return roleRepository.save(role);
                });

        BaseUser admin = BaseUser.builder()
                .firstName("Super")
                .lastName("Admin")
                .industryName("System")
                .businessName("BrandLocus")
                .email(adminEmail)
                .password(passwordEncoder.encode("Admin@123")) // default password
                .role(adminRole)
                .isActive(true)
                .userStatus(UserStatus.ACTIVE.getValue())
                .agreementToReceiveAIGeneratedResponse(false)
                .agreementToReceiveAIGeneratedResponseTimestamp(LocalDateTime.now())
                .build();

        baseUserRepository.save(admin);
        log.info("✅ Default admin user created -> email: {}, password: {}", adminEmail, "Admin@123");
    }
}
