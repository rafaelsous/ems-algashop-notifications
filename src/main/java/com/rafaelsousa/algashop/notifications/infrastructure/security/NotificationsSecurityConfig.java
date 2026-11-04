package com.rafaelsousa.algashop.notifications.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class NotificationsSecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) {
		http.csrf(AbstractHttpConfigurer::disable)
			.cors(AbstractHttpConfigurer::disable)
			.sessionManagement(
				sessionManagement ->
					sessionManagement.sessionCreationPolicy(
						SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(
				auth ->
					auth.requestMatchers("/actuator/health/**", "/actuator/info/**")
						.permitAll()
						.anyRequest()
						.authenticated())
			.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

		return http.build();
	}
}
