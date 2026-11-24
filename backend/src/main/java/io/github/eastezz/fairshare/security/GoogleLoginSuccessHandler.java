package io.github.eastezz.fairshare.security;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;

import io.github.eastezz.fairshare.auth.GoogleAccountService;
import io.github.eastezz.fairshare.auth.GoogleEmailNotVerifiedException;
import io.github.eastezz.fairshare.user.UserAccount;

/**
 * Runs after Google has confirmed who the person is. It finds or creates our account
 * and then logs that account in exactly like a password login (the session's user name is our email),
 * so everything else in the app (CurrentUser, own groups) works the same for both ways of logging in.
 */
@Component
public class GoogleLoginSuccessHandler implements AuthenticationSuccessHandler {

	private final GoogleAccountService googleAccountService;

	private final SecurityContextRepository securityContextRepository;

	public GoogleLoginSuccessHandler(GoogleAccountService googleAccountService,
			SecurityContextRepository securityContextRepository) {
		this.googleAccountService = googleAccountService;
		this.securityContextRepository = securityContextRepository;
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException {
		OidcUser google = (OidcUser) authentication.getPrincipal();
		UserAccount account;
		try {
			account = googleAccountService.signIn(google.getSubject(), google.getEmail(),
					Boolean.TRUE.equals(google.getEmailVerified()));
		}
		catch (GoogleEmailNotVerifiedException ex) {
			SecurityContextHolder.clearContext();
			response.sendRedirect("/login?error=google");
			return;
		}

		Authentication ours = UsernamePasswordAuthenticationToken.authenticated(account.getEmail(), null,
				List.of(new SimpleGrantedAuthority("ROLE_USER")));
		SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(ours);
		SecurityContextHolder.setContext(context);
		securityContextRepository.saveContext(context, request, response);

		// Back to the start page of the React app.
		response.sendRedirect("/");
	}

}