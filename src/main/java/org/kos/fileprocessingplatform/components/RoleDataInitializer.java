package org.kos.fileprocessingplatform.components;

import lombok.RequiredArgsConstructor;
import org.kos.fileprocessingplatform.models.ERole;
import org.kos.fileprocessingplatform.models.RoleEntity;
import org.kos.fileprocessingplatform.repositories.RoleEntityRepo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoleDataInitializer implements CommandLineRunner {

    private final RoleEntityRepo roleEntityRepo;

    @Override
    public void run(String... args) {
        createRoleIfNotExists(ERole.ROLE_USER);
        createRoleIfNotExists(ERole.ROLE_ADMIN);
    }

    private void createRoleIfNotExists(ERole roleName) {
        roleEntityRepo.findByName(roleName)
                .orElseGet(() -> roleEntityRepo.save(
                        RoleEntity.builder()
                                .name(roleName)
                                .build()
                ));
    }
}
