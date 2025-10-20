package com.pneumaliback.www.service;

import com.pneumaliback.www.dto.AuthResponse;
import com.pneumaliback.www.dto.LoginRequest;
import com.pneumaliback.www.dto.MessageResponse;
import com.pneumaliback.www.dto.RegisterRequest;
import com.pneumaliback.www.dto.VerificationRequest;
import com.pneumaliback.www.dto.ResendVerificationRequest;
import com.pneumaliback.www.dto.ForgotPasswordRequest;
import com.pneumaliback.www.dto.ResetPasswordRequest;
import com.pneumaliback.www.entity.RefreshToken;
import com.pneumaliback.www.entity.User;
import com.pneumaliback.www.enums.Role;
import com.pneumaliback.www.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.security.SecureRandom;

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
    private final MailService mailService;

    @Transactional
    public MessageResponse register(RegisterRequest request) {
        // Vérification de l'unicité de l'email
        if (userRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Un utilisateur avec cet email existe déjà");
        }

        // Vérification de la confirmation du mot de passe
        if (!request.password().equals(request.confirmPassword())) {
            throw new RuntimeException("Les mots de passe ne correspondent pas");
        }

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
                .enabled(false)
                .failedAttempts(0)
                .lockTime(null)
                .build();

        User savedUser = userRepository.save(user);
        sendNewVerificationCode(savedUser);
        return new MessageResponse("Inscription réussie. Un code de vérification a été envoyé à votre email.");
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

        } catch (DisabledException e) {
            throw new RuntimeException("Compte non activé. Veuillez vérifier votre email.");
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

    @Transactional
    public MessageResponse resendVerificationCode(ResendVerificationRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (user.isEnabled()) {
            throw new RuntimeException("Compte déjà activé");
        }

        Instant now = Instant.now();
        if (user.getVerificationSentAt() != null && now.isBefore(user.getVerificationSentAt().plusSeconds(20))) {
            throw new RuntimeException("Veuillez patienter avant de renvoyer le code");
        }

        sendNewVerificationCode(user);
        return new MessageResponse("Nouveau code envoyé à votre email.");
    }

    @Transactional
    public AuthResponse verifyEmail(VerificationRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (user.isEnabled()) {
            throw new RuntimeException("Compte déjà activé");
        }

        if (user.getVerificationCode() == null || user.getVerificationExpiry() == null) {
            throw new RuntimeException("Aucun code de vérification actif");
        }

        if (!user.getVerificationCode().equals(request.code())) {
            throw new RuntimeException("Code de vérification invalide");
        }

        if (Instant.now().isAfter(user.getVerificationExpiry())) {
            throw new RuntimeException("Code de vérification expiré");
        }

        user.setEnabled(true);
        user.setVerificationCode(null);
        user.setVerificationExpiry(null);
        userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = jwtService.generateToken(userDetails);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

        return buildAuthResponse(accessToken, refreshToken.getToken(), user);
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

    private String generateVerificationCode() {
        SecureRandom random = new SecureRandom();
        int number = random.nextInt(1_000_000);
        return String.format("%06d", number);
    }

    private void sendNewVerificationCode(User user) {
        String code = generateVerificationCode();
        Instant expiry = Instant.now().plus(15, ChronoUnit.MINUTES);
        user.setVerificationCode(code);
        user.setVerificationExpiry(expiry);
        user.setVerificationSentAt(Instant.now());
        userRepository.save(user);
        mailService.sendVerificationEmail(user.getEmail(), code);
    }

    @Transactional
    public MessageResponse requestPasswordReset(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (!user.isEnabled()) {
            throw new RuntimeException("Compte non activé");
        }

        Instant now = Instant.now();
        if (user.getResetSentAt() != null && now.isBefore(user.getResetSentAt().plusSeconds(20))) {
            throw new RuntimeException("Veuillez patienter avant de renvoyer le code");
        }

        sendNewResetCode(user);
        return new MessageResponse("Un code de réinitialisation a été envoyé à votre email.");
    }

    @Transactional
    public MessageResponse confirmPasswordReset(ResetPasswordRequest request) {
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new RuntimeException("Les mots de passe ne correspondent pas");
        }

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (!user.isEnabled()) {
            throw new RuntimeException("Compte non activé");
        }

        if (user.getResetCode() == null || user.getResetExpiry() == null) {
            throw new RuntimeException("Aucun code de réinitialisation actif");
        }

        if (!user.getResetCode().equals(request.code())) {
            throw new RuntimeException("Code de réinitialisation invalide");
        }

        if (Instant.now().isAfter(user.getResetExpiry())) {
            throw new RuntimeException("Code de réinitialisation expiré");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setResetCode(null);
        user.setResetExpiry(null);
        user.setResetSentAt(null);
        userRepository.save(user);

        return new MessageResponse("Mot de passe réinitialisé avec succès.");
    }

    private void sendNewResetCode(User user) {
        String code = generateVerificationCode();
        Instant expiry = Instant.now().plus(15, ChronoUnit.MINUTES);
        user.setResetCode(code);
        user.setResetExpiry(expiry);
        user.setResetSentAt(Instant.now());
        userRepository.save(user);
        mailService.sendPasswordResetEmail(user.getEmail(), code);
    }
}
