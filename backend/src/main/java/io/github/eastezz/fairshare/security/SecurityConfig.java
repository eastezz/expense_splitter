package io.github.eastezz.fairshare.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

/**
 * The security rules of the app (Spring Security).
 * For now every request is still allowed; the next piece requires a login for the API.
 */
@Configuration
public class SecurityConfig {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http, SecurityContextRepository securityContextRepository)
			throws Exception {
		http
			.authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
			// CSRF protection is switched on in the next piece, together with locking the API.
			.csrf(csrf -> csrf.disable())
			// Reads the login of each request from the same place where AuthController saves it.
			.securityContext(context -> context.securityContextRepository(securityContextRepository))
			// POST /api/auth/logout ends the session, deletes the cookie and answers 204 No Content.
			.logout(logout -> logout
				.logoutUrl("/api/auth/logout")
				.deleteCookies("JSESSIONID")
				.logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT)));
		return http.build();
	}

	/**
	 * Turns passwords into BCrypt hashes and checks a password against a hash.
	 * BCrypt adds a random salt and is slow on purpose, which makes guessing passwords expensive.
	 */
	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	/**
	 * Checks an email and a password: finds the account with AccountUserDetailsService
	 * and compares the password with the stored hash.
	 */
	@Bean
	AuthenticationManager authenticationManager(UserDetailsService userDetailsService,
			PasswordEncoder passwordEncoder) {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
		provider.setPasswordEncoder(passwordEncoder);
		return new ProviderManager(provider);
	}

	/**
	 * Where the login is remembered between requests: in the server-side session.
	 * The browser only gets a random session id in the cookie JSESSIONID.
	 */
	@Bean
	SecurityContextRepository securityContextRepository() {
		return new HttpSessionSecurityContextRepository();
	}

}