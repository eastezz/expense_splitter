package io.github.eastezz.fairshare.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * The security rules of the app (Spring Security).
 * For now every request is allowed, exactly like before Spring Security was added;
 * the next pieces of step 12 add accounts and then require a login for the API.
 */
@Configuration
public class SecurityConfig {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
			// CSRF protection only matters once there is a login cookie; it is switched on in that piece.
			.csrf(csrf -> csrf.disable());
		return http.build();
	}

}