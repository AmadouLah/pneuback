package com.pneumaliback.www.service;

import com.pneumaliback.www.dto.AuthResponse;
import com.pneumaliback.www.dto.LoginRequest;
import com.pneumaliback.www.dto.RegisterRequest;
import com.pneumaliback.www.entity.RefreshToken;
import com.pneumaliback.www.entity.User;
import com.pneumaliback.www.enums.Role;
import com.pneumaliback.www.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Vérification de l'unicité de l'email
        if (userRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Un utilisateur avec cet email existe déjà");
        }

        // Vérification de la confirmation du mot de passe
        if (!request.password().equals(request.confirmPassword())) {
            throw new RuntimeException("Les mots de passe ne correspondent pas");
        }

        // Création de l'utilisateur
        User user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .phoneNumber(request.phoneNumber())
                .role(Role.CLIENT)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .enabled(true)
                .failedAttempts(0)
                .lockTime(null)
                .build();

        User savedUser = userRepository.save(user);

        // Génération des tokens
        UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getEmail());
        String accessToken = jwtService.generateToken(userDetails);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(savedUser);

        return buildAuthResponse(accessToken, refreshToken.getToken(), savedUser);
    }

    public AuthResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            // Réinitialisation des tentatives échouées
            if (user.getFailedAttempts() > 0) {
                user.setFailedAttempts(0);
                user.setLockTime(null);
                userRepository.save(user);
            }

            // Génération des tokens
            String accessToken = jwtService.generateToken(userDetails);
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

            return buildAuthResponse(accessToken, refreshToken.getToken(), user);

        } catch (BadCredentialsException e) {
            handleFailedLogin(request.email());
            throw new RuntimeException("Email ou mot de passe incorrect");
        } catch (LockedException e) {
            throw new RuntimeException("Compte verrouillé. Réessayez plus tard.");
        }
    }

    public AuthResponse refreshToken(String refreshToken) {
        RefreshToken token = refreshTokenService.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Refresh token invalide"));

        token = refreshTokenService.verifyExpiration(token);

        UserDetails userDetails = userDetailsService.loadUserByUsername(token.getUser().getEmail());
        String newAccessToken = jwtService.generateToken(userDetails);

        return buildAuthResponse(newAccessToken, refreshToken, token.getUser());
    }

    public void logout(String refreshToken) {
        RefreshToken token = refreshTokenService.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Refresh token invalide"));

        refreshTokenService.revokeByUser(token.getUser());
    }

    private void handleFailedLogin(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            user.setFailedAttempts(user.getFailedAttempts() + 1);

            if (user.getFailedAttempts() >= 5) {
                user.setAccountNonLocked(false);
                user.setLockTime(Instant.now());
            }

            userRepository.save(user);
        });
    }

    private AuthResponse buildAuthResponse(String accessToken, String refreshToken, User user) {
        return new AuthResponse(
                accessToken,
                refreshToken,
                "Bearer",
                86400000L, // 24 heures
                new AuthResponse.UserInfo(
                        user.getId(),
                        user.getEmail(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getRole().name()));
    }
}
