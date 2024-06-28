package com.jsp.onlineshoppingapplication.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.jsp.onlineshoppingapplication.repository.UserRepository;

import lombok.AllArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@AllArgsConstructor
public class SecurityConfig {

    private final UserRepository userRepository;

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity.csrf(AbstractHttpConfigurer::disable)
//                .securityMatchers(matcher -> matcher.requestMatchers("/api/v1/sellers/register",
//                        "/api/v1/customers/register", "/api/v1/users/otpVerification"))
//                .authorizeHttpRequests(authorize -> authorize.requestMatchers("/api/v1/sellers/register",
//                        "/api/v1/customers/register", "/api/v1/users/otpVerification", "/api/v1/createJwtToken")
//                        .permitAll()
//                        .anyRequest()
//                        .authenticated())

                .authorizeHttpRequests(authorize -> authorize.anyRequest()
                        .permitAll())
//                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                .addFilterBefore(new ClientApiKeyFilter(userRepository), UsernamePasswordAuthenticationFilter.class)
                .formLogin(Customizer.withDefaults())
                .build();
    }

 
}