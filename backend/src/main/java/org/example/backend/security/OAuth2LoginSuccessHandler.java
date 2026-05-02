package org.example.backend.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.example.backend.domain.entity.Role;
import org.example.backend.domain.entity.User;
import org.example.backend.domain.enums.RoleName;
import org.example.backend.domain.enums.UserStatus;
import org.example.backend.repository.RoleRepository;
import org.example.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;

    @Value("${app.oauth2.redirect-uri:http://localhost:4200/login}")
    private String redirectUri;

    public OAuth2LoginSuccessHandler(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            CustomUserDetailsService customUserDetailsService
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = firstNonBlank(
                attribute(oAuth2User, "email"),
                attribute(oAuth2User, "emailAddress"),
                attribute(oAuth2User, "preferred_username")
        );
        String firstName = firstNonBlank(attribute(oAuth2User, "given_name"), attribute(oAuth2User, "localizedFirstName"));
        String lastName = firstNonBlank(attribute(oAuth2User, "family_name"), attribute(oAuth2User, "localizedLastName"));

        if (!StringUtils.hasText(email)) {
            response.sendRedirect(redirectUri + "?error=linkedin_missing_email");
            return;
        }

        User user = userRepository.findByEmail(email).orElseGet(() -> createLinkedInUser(email, firstName, lastName));
        String role = resolvePrimaryRole(user);
        String token = jwtService.generateToken(
                customUserDetailsService.loadUserByUsername(user.getEmail()),
                Map.of("role", role)
        );

        String url = redirectUri + "?token=" + token + "&email=" + user.getEmail() + "&role=" + role;
        response.sendRedirect(url);
    }

    private User createLinkedInUser(String email, String firstName, String lastName) {
        Role clientRole = roleRepository.findByName(RoleName.ROLE_CLIENT).orElseThrow();
        User user = new User();
        user.setFirstName(StringUtils.hasText(firstName) ? firstName : "Client");
        user.setLastName(StringUtils.hasText(lastName) ? lastName : "LinkedIn");
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setStatus(UserStatus.ACTIVE);
        user.setRoles(Set.of(clientRole));
        return userRepository.save(user);
    }

    private String resolvePrimaryRole(User user) {
        boolean isAdmin = user.getRoles().stream().anyMatch(r -> r.getName() == RoleName.ROLE_ADMIN);
        return isAdmin ? RoleName.ROLE_ADMIN.name() : RoleName.ROLE_CLIENT.name();
    }

    private String attribute(OAuth2User user, String key) {
        Object value = user.getAttributes().get(key);
        return value == null ? "" : String.valueOf(value);
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return "";
    }
}
