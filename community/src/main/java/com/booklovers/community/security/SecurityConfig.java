package com.booklovers.community.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // wymagane przez specyfikację
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // wyłączamy CSRF (dla uproszczenia przy REST API i konsoli H2)
            .csrf(csrf -> csrf.disable())
            
            // konfiguracja nagłówków dla konsoli H2 (ona używa ramek, które Spring blokuje)
            .headers(headers -> headers.frameOptions(frame -> frame.disable()))
            
            // reguły dostępu do endpointów
            .authorizeHttpRequests(auth -> auth
                // Publiczne endpointy (Rejestracja, Swagger, Baza danych H2)
                .requestMatchers("/api/v1/auth/**", "/h2-console/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                
                // publiczny dostęp do podglądu książek (opcjonalnie)
                .requestMatchers("/api/v1/books/**").permitAll() 
                
                // wszystko inne wymaga logowania
                .anyRequest().authenticated()
            )
            
            // włączamy formularz logowania (dla przeglądarki)
            .formLogin(withDefaults())
            
            // włączamy Basic Auth (dla testowania w Postmanie)
            .httpBasic(withDefaults());

        return http.build();
    }

    // Bean AuthenticationManager (przydaje się przy własnym endpointcie logowania)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
