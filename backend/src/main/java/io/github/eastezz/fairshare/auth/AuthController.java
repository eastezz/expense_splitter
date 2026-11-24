package io.github.eastezz.fairshare.auth;

import java.net.URI;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoints for accounts under /api/auth.
 * POST /api/auth/logout is handled by Spring Security itself (see SecurityConfig).
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;

	private final SecurityContextRepository securityContextRepository;

	private final ObjectProvider<ClientRegistrationRepository> googleLogin;

	public AuthController(AuthService authService, SecurityContextRepository securityContextRepository,
			ObjectProvider<ClientRegistrationRepository> googleLogin) {
		this.authService = authService;
		this.securityContextRepository = securityContextRepository;
		this.googleLogin = googleLogin;
	}

	/**
	 * GET /api/auth/providers: which other ways of logging in are switched on, e.g. {"google":true}.
	 * The login page only shows the "Sign in with Google" button when Google is configured.
	 */
	@GetMapping("/providers")
	public Map<String, Boolean> providers() {
		return Map.of("google", googleLogin.getIfAvailable() != null);
	}

	/** POST /api/auth/register: creates an account, answers 201 Created (without any password). */
	@PostMapping("/register")
	public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
		UserResponse created = authService.register(request);
		return ResponseEntity.created(URI.create("/api/auth/me")).body(created);
	}

	/**
	 * POST /api/auth/login: checks email and password, then remembers the user in the session.
	 * The browser gets the session id as the cookie JSESSIONID and sends it with every later request.
	 */
	@PostMapping("/login")
	public UserResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) {
		Authentication authentication = authService.authenticate(request);

		// Always start a new session at login, with a new id ("session fixation" protection):
		// a session id that someone else planted before the login becomes worthless.
		HttpSession oldSession = httpRequest.getSession(false);
		if (oldSession != null) {
			oldSession.invalidate();
		}
		httpRequest.getSession(true);

		SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(authentication);
		SecurityContextHolder.setContext(context);
		securityContextRepository.saveContext(context, httpRequest, httpResponse);

		return authService.currentUser(authentication.getName());
	}

	/** GET /api/auth/me: the logged-in user, or 401 if nobody is logged in. */
	@GetMapping("/me")
	public UserResponse me(Authentication authentication) {
		if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
			throw new NotLoggedInException();
		}
		return authService.currentUser(authentication.getName());
	}

}