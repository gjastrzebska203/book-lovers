package com.booklovers.community.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
        return new BCryptPasswordEncoder(); 
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

                // zasoby statyczne
                .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()

                // H2 Console
                .requestMatchers("/h2-console/**").permitAll()

                // swagger
                .requestMatchers(
                    "/v3/api-docs",      
                    "/v3/api-docs/**",    
                    "/swagger-ui/**",
                    "/swagger-ui.html"
                ).hasRole("ADMIN")
                
                // publiczne widoki HTML (Strona główna, Rejestracja, Logowanie)
                .requestMatchers("/", "/index", "/register", "/login").permitAll()
                .requestMatchers(HttpMethod.GET, "/books/**").permitAll()
                
                // publiczne API (Endpointy REST)
                .requestMatchers(HttpMethod.POST, "/api/v1/users/register").permitAll() 
                .requestMatchers(HttpMethod.GET, "/api/v1/books/**").permitAll()      
                .requestMatchers(HttpMethod.GET, "/api/v1/authors/**").permitAll()    
                .requestMatchers(HttpMethod.GET, "/api/v1/books/*/reviews").permitAll() 

                // zarządznie książkami
                .requestMatchers(HttpMethod.POST, "/api/v1/books/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/books/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/books/**").hasRole("ADMIN")

                // zarządzanie autorami
                .requestMatchers(HttpMethod.POST, "/api/v1/authors/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/v1/authors/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/authors/**").hasRole("ADMIN")

                // zarządzanie recenzjami
                .requestMatchers("/api/v1/admin/reviews/**").hasRole("ADMIN")

                // panel admina
                .requestMatchers("/admin/**").hasRole("ADMIN")

                .requestMatchers(HttpMethod.DELETE, "/api/v1/users/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/users/*/toggle-block").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            
            // konfiguracja formularza logowania
            .formLogin(login -> login
                .loginPage("/login")       
                .loginProcessingUrl("/login") 
                .defaultSuccessUrl("/", true) 
                .permitAll()
            )
            
            // konfiguracja wylogowania
            .logout(logout -> logout
                .logoutUrl("/logout") 
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )

            .httpBasic(withDefaults());

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
