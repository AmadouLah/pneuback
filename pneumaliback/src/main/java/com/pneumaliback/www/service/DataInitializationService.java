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
        int updated = 0;
        for (DefaultUser du : defaults) {
            String email = du.email().trim();
            var existingOpt = userRepository.findByEmailIgnoreCase(email);
            if (existingOpt.isEmpty()) {
                User user = buildUser(du);
                userRepository.save(user);
                created++;
                log.info("Utilisateur par défaut créé: email={}, role={}", email, du.role());
                continue;
            }

            User existing = existingOpt.get();
            boolean needUpdate = false;
            if (!passwordEncoder.matches(du.rawPassword(), existing.getPassword())) {
                existing.setPassword(passwordEncoder.encode(du.rawPassword()));
                needUpdate = true;
            }
            if (!existing.isEnabled()) { existing.setEnabled(true); needUpdate = true; }
            if (!existing.isAccountNonLocked()) { existing.setAccountNonLocked(true); existing.setFailedAttempts(0); existing.setLockTime(null); needUpdate = true; }
            if (existing.getRole() != du.role()) { existing.setRole(du.role()); needUpdate = true; }

            if (needUpdate) {
                userRepository.save(existing);
                updated++;
                log.info("Utilisateur par défaut mis à jour: email={}, role={}, unlocked={}, enabled={}", email, existing.getRole(), existing.isAccountNonLocked(), existing.isEnabled());
            }
        }

        if (created > 0 || updated > 0) {
            log.info("Synthèse init users -> créés: {}, mis à jour: {}", created, updated);
        } else {
            log.info("Utilisateurs par défaut déjà conformes.");
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
