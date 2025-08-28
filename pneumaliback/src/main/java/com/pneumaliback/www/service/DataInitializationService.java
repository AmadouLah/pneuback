package com.pneumaliback.www.service;

import com.pneumaliback.www.entity.User;
import com.pneumaliback.www.enums.Role;
import com.pneumaliback.www.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
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
        if (userRepository.count() == 0) {
            log.info("Aucun utilisateur trouvé. Création des utilisateurs par défaut...");

            List<User> defaultUsers = Arrays.asList(
                    createDefaultUser(
                            "admin@pneumali.ml",
                            "admin123",
                            "Admin",
                            "PneuMali",
                            "+22312345678",
                            Role.ADMIN,
                            "Compte administrateur par défaut"),
                    createDefaultUser(
                            "client@pneumali.ml",
                            "client123",
                            "Client",
                            "Demo",
                            "+22312345679",
                            Role.CLIENT,
                            "Compte client de démonstration"),
                    createDefaultUser(
                            "influenceur@pneumali.ml",
                            "influenceur123",
                            "Influenceur",
                            "Demo",
                            "+22312345680",
                            Role.INFLUENCEUR,
                            "Compte influenceur de démonstration"));

            userRepository.saveAll(defaultUsers);
            log.info("{} utilisateurs par défaut créés avec succès.", defaultUsers.size());

            // Affichage des informations de connexion
            log.info("=== UTILISATEURS PAR DÉFAUT CRÉÉS ===");
            log.info("ADMIN: {} / {}", "admin@pneumali.ml", "admin123");
            log.info("CLIENT: {} / {}", "client@pneumali.ml", "client123");
            log.info("INFLUENCEUR: {} / {}", "influenceur@pneumali.ml", "influenceur123");
            log.info("=====================================");

        } else {
            log.info("Des utilisateurs existent déjà. Aucun utilisateur par défaut créé.");
        }
    }

    private User createDefaultUser(String email, String password, String firstName,
            String lastName, String phoneNumber, Role role, String description) {
        return User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .firstName(firstName)
                .lastName(lastName)
                .phoneNumber(phoneNumber)
                .role(role)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .enabled(true)
                .failedAttempts(0)
                .lockTime(null)
                .build();
    }
}
