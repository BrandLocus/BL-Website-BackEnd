package com.hvc.brandlocus.seeder;

import com.hvc.brandlocus.entities.Roles;
import com.hvc.brandlocus.enums.UserRoles;
import com.hvc.brandlocus.repositories.RolesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RoleSeeder implements CommandLineRunner {

    private final RolesRepository rolesRepository;

    @Override
    public void run(String... args) {
        seedRoles();
    }

    private void seedRoles() {
        for (UserRoles type : UserRoles.values()) {
            String roleName = type.name();

            boolean exists = rolesRepository.findByName(roleName).isPresent();
            if (!exists) {
                Roles role = Roles.builder().name(roleName).build();
                rolesRepository.save(role);
                log.info("✅ Seeded role: {}", roleName);
            } else {
                log.info("ℹ️ Role already exists: {}", roleName);
            }
        }
    }
}
