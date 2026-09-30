
package com.taskflow.taskflowpro.security;

import com.taskflow.taskflowpro.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .userDetailsService(userDetailsService)

                .authorizeHttpRequests(auth -> auth

                        // Public pages
                        .requestMatchers(
                                "/auth/**",
                                "/css/**",
                                "/js/**"
                        ).permitAll()

                        // Faculty and Admin only
                        .requestMatchers(
                                "/assignments/create",
                                "/assignments/save",
                                "/assignments/delete/**",
                                "/assignments/*/submissions",
                                "/assignments/submissions/*",
                                "/assignments/submissions/*/evaluate",
                                "/assignments/submissions/*/download"
                        ).hasAnyRole("FACULTY", "ADMIN")

                        // Students, Faculty and Admin
                        // can view/use normal assignment pages
                        .requestMatchers(
                                "/assignments/**"
                        ).authenticated()

                        // Everything else requires login
                        .anyRequest().authenticated()
                )

                .formLogin(form -> form
                        .loginPage("/auth/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/")
                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutSuccessUrl("/auth/login?logout")
                        .permitAll()
                );

        return http.build();
    }
}
