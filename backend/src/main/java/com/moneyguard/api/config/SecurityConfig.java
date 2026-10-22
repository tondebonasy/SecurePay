package com.moneyguard.api.config;

import com.moneyguard.api.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
        http.csrf(csrf -> csrf.disable());
        http.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.httpBasic(basic -> basic.disable());
        http.exceptionHandling(errors -> errors
          .authenticationEntryPoint((req,res,e)->{res.setStatus(401);res.setContentType("application/json");res.getWriter().write("{\"error\":{\"code\":\"UNAUTHORIZED\",\"message\":\"Authentication required\"}}");})
          .accessDeniedHandler((req,res,e)->{res.setStatus(403);res.setContentType("application/json");res.getWriter().write("{\"error\":{\"code\":\"FORBIDDEN\",\"message\":\"Administrator permission required\"}}");}));

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health", "/v1/health").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/v1/webhooks/**").permitAll()
                .requestMatchers("/v1/auth/**").permitAll()
                .requestMatchers("/v1/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
        );

        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
