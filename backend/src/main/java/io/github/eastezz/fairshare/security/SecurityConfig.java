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
import org.springframework.security.web.csrf.CsrfFilter;

/**
 * The security rules of the app (Spring Security):
 * the API needs a login (except health check and the account endpoints),
 * and every request that changes data needs the CSRF token.
 */
@Configuration
public class SecurityConfig {

	/** The answer for a request to the API without login: 401 with the same JSON format as our other errors. */
	private static final String NOT_LOGGED_IN_JSON =
			"{\"title\":\"Unauthorized\",\"status\":401,\"detail\":\"Not logged in\"}";

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http, SecurityContextRepository securityContextRepository)
			throws Exception {
		http
			.authorizeHttpRequests(auth -> auth
				// Open to everyone: the health check and register, login, logout, "who am I".
				.requestMatchers("/api/health", "/api/auth/**").permitAll()
				// All other API endpoints (groups, members, expenses, …) only after login.
				.requestMatchers("/api/**").authenticated()
				// Everything else (Swagger UI, API docs, H2 console in development) stays open.
				.anyRequest().permitAll())
			// CSRF protection for a single-page app: the token goes into the cookie XSRF-TOKEN,
			// and the React app sends it back in the header X-XSRF-TOKEN (see withCsrf in api.ts).
			// The H2 console is a development tool with its own forms, so it is left out.
			.csrf(csrf -> csrf.spa().ignoringRequestMatchers("/h2-console/**"))
			// Puts the CSRF token into the cookie on every answer, so it is there before the first POST.
			.addFilterAfter(new CsrfCookieFilter(), CsrfFilter.class)
			// The H2 console shows itself in frames; allow that only from our own site.
			.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
			// Without login: 401 with a JSON message instead of Spring's default login form.
			.exceptionHandling(exceptions -> exceptions.authenticationEntryPoint((request, response, ex) -> {
				response.setStatus(HttpStatus.UNAUTHORIZED.value());
				response.setContentType("application/problem+json");
				response.getWriter().write(NOT_LOGGED_IN_JSON);
			}))
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