package com.pneumaliback.www.service;

import com.pneumaliback.www.entity.User;
import com.pneumaliback.www.enums.Role;
import com.pneumaliback.www.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataInitializationService implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        log.info("Début de l'initialisation des données par défaut...");
        initializeDefaultUsers();
        log.info("Initialisation des données par défaut terminée.");
    }

    private void initializeDefaultUsers() {
        List<DefaultUser> defaults = Arrays.asList(
                new DefaultUser(
                        "admin@pneumali.ml",
                        "Admin@2024!",
                        "Admin",
                        "PneuMali",
                        "+22312345678",
                        Role.ADMIN),
                new DefaultUser(
                        "client@pneumali.ml",
                        "Client#2024!",
                        "Client",
                        "Demo",
                        "+22312345679",
                        Role.CLIENT),
                new DefaultUser(
                        "influenceur@pneumali.ml",
                        "Influenc3ur!2024",
                        "Influenceur",
                        "Demo",
                        "+22312345680",
                        Role.INFLUENCEUR));

        int created = 0;
        for (DefaultUser du : defaults) {
            if (userRepository.existsByEmail(du.email())) {
                continue;
            }
            User user = buildUser(du);
            userRepository.save(user);
            created++;
            log.info("Utilisateur par défaut créé: email={}, role={}", du.email(), du.role());
        }

        if (created == 0) {
            log.info("Aucun utilisateur par défaut à créer (déjà présents).");
        } else {
            log.info("{} utilisateur(s) par défaut créé(s).", created);
        }
    }

    private User buildUser(DefaultUser du) {
        return User.builder()
                .email(du.email())
                .password(passwordEncoder.encode(du.rawPassword()))
                .firstName(du.firstName())
                .lastName(du.lastName())
                .phoneNumber(du.phone())
                .role(du.role())
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .enabled(true)
                .failedAttempts(0)
                .lockTime(null)
                .build();
    }

    private record DefaultUser(
            String email,
            String rawPassword,
            String firstName,
            String lastName,
            String phone,
            Role role) {
    }
}
