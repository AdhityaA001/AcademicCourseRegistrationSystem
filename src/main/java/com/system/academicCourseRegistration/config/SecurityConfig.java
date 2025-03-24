package com.system.academicCourseRegistration.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

import com.system.academicCourseRegistration.repository.UsersRepository;
import com.system.academicCourseRegistration.service.CustomUserDetailsService;

@Configuration
public class SecurityConfig {

    @Bean
    UserDetailsService userDetailsService(UsersRepository userRepository) {
        return new CustomUserDetailsService(userRepository, passwordEncoder());
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/admin/**").hasAuthority("ADMIN")
                .requestMatchers("/professor/**").hasAuthority("PROFESSOR")
                .requestMatchers("/student/**").hasAuthority("STUDENT")
                .anyRequest().authenticated()
            )
            .httpBasic(withDefaults());

        return http.build();
    }

}
