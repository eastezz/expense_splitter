package io.github.eastezz.fairshare.user;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import io.github.eastezz.fairshare.auth.NotLoggedInException;

/**
 * Finds the account of the user who sent the current request.
 * Spring Security keeps the logged-in user of each request in the SecurityContextHolder;
 * its "name" is the email we saved at login.
 */
@Component
public class CurrentUser {

	private final UserAccountRepository userAccountRepository;

	public CurrentUser(UserAccountRepository userAccountRepository) {
		this.userAccountRepository = userAccountRepository;
	}

	/** The account of the logged-in user, or NotLoggedInException (401) if there is none. */
	public UserAccount require() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
			throw new NotLoggedInException();
		}
		return userAccountRepository.findByEmail(authentication.getName()).orElseThrow(NotLoggedInException::new);
	}

}