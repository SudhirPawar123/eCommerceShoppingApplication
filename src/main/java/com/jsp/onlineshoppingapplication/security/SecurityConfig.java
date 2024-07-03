package com.jsp.onlineshoppingapplication.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.jsp.onlineshoppingapplication.repository.RefreshTokenRepository;
import com.jsp.onlineshoppingapplication.securityfilters.LoginFilter;
import com.jsp.onlineshoppingapplication.securityfilters.RefreshFilter;
import com.jsp.onlineshoppingapplication.securityfilters.SecurityFilter;

import lombok.AllArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@AllArgsConstructor
public class SecurityConfig {

	private final JwtService jwtService;
	private final RefreshTokenRepository refreshTokenRepository;

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder(12);
	}

	 @Bean
	    @Order(3)
	    SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
	        return httpSecurity.csrf(AbstractHttpConfigurer::disable)
	                .securityMatchers(match -> match.requestMatchers("/api/v1/**"))
	                .authorizeHttpRequests(authorize -> authorize.requestMatchers("/api/v1/login/**",
	                                "api/v1/otpverification/**",
	                                "api/v1/sellers/register/**",
	                                "api/v1/customers/register/**")
	                        .permitAll()
	                        .anyRequest()
	                        .authenticated())
	                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
	                .addFilterBefore(new SecurityFilter(jwtService), UsernamePasswordAuthenticationFilter.class)
	                .build();
	    }

	    @Bean
	    @Order(2)
	    SecurityFilterChain securityFilterChainRefreshFilter(HttpSecurity httpSecurity) throws Exception {
	        return httpSecurity.csrf(AbstractHttpConfigurer::disable)
	                .securityMatchers(match -> match.requestMatchers("/api/v1/refreshLogin/**"))
	                .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
	                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
	                .addFilterBefore(new RefreshFilter(jwtService, refreshTokenRepository), UsernamePasswordAuthenticationFilter.class)
	                .build();
	    }

	@Bean
	@Order(1)
	SecurityFilterChain securityFilterChain1(HttpSecurity httpSecurity) throws Exception {
		return httpSecurity.csrf(AbstractHttpConfigurer::disable)
				.securityMatchers(match -> match.requestMatchers("/api/v1/login/**"))
				.authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.addFilterBefore(new LoginFilter(), UsernamePasswordAuthenticationFilter.class).build();
	}

	@Bean
	AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
			throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}

}