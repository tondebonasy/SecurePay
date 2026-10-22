package com.moneyguard.api.security;

import com.moneyguard.api.repo.UserRepo;
import com.moneyguard.api.web.ApiException;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwt;
    private final UserRepo userRepo;
    private final com.moneyguard.api.repo.DeviceSessionRepo sessions;

    public JwtAuthFilter(JwtService jwt, UserRepo userRepo, com.moneyguard.api.repo.DeviceSessionRepo sessions) {
        this.jwt = jwt;
        this.userRepo = userRepo;
        this.sessions = sessions;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String auth = request.getHeader("Authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);
            try {
                Claims claims = jwt.parse(token);
                String userId = claims.getSubject();
                String did = claims.get("did", String.class);
                var dbUser = userRepo.findById(userId)
                        .filter(u -> "ACTIVE".equalsIgnoreCase(u.getStatus()))
                        .orElseThrow(() -> ApiException.unauthorized("USER_INACTIVE", "User account is not active"));
                List<String> roles = List.of(dbUser.getRoles().split(",")).stream()
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toList();

                // Device binding: if token contains did, require X-Device-Id match
                if (did != null) {
                    var session = sessions.findByUserIdAndDeviceIdAndRevokedAtIsNull(userId, did).filter(s -> s.getRefreshExpiresAt().isAfter(java.time.Instant.now())).orElseThrow(() -> ApiException.unauthorized("SESSION_INVALID", "Session is no longer active"));
                    String headerDid = request.getHeader("X-Device-Id");
                    if (headerDid == null || !did.equals(headerDid)) throw ApiException.unauthorized("DEVICE_MISMATCH", "Device mismatch");
                }

                AuthUser principal = new AuthUser(userId, roles == null ? List.of() : roles, did);
                var authToken = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } catch (Exception ignored) {
                // Let downstream handle as unauthenticated; don't hard fail health/auth routes.
            }
        }
        filterChain.doFilter(request, response);
    }
}
