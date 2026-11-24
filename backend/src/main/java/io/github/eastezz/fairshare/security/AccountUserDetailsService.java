package io.github.eastezz.fairshare.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import io.github.eastezz.fairshare.user.UserAccount;
import io.github.eastezz.fairshare.user.UserAccountRepository;

/**
 * Tells Spring Security how to find an account at login: by email, in our users table.
 * Spring then compares the typed password with the stored BCrypt hash itself.
 */
@Service
public class AccountUserDetailsService implements UserDetailsService {

	private final UserAccountRepository userAccountRepository;

	public AccountUserDetailsService(UserAccountRepository userAccountRepository) {
		this.userAccountRepository = userAccountRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String email) {
		UserAccount account = userAccountRepository.findByEmail(UserAccount.normalizeEmail(email))
			.orElseThrow(() -> new UsernameNotFoundException("No account for " + email));
		if (account.getPasswordHash() == null) {
			// This account only signs in with Google, so a password can't be right.
			// To the user it looks exactly like a wrong password ("Wrong email or password").
			throw new UsernameNotFoundException("No password for " + email);
		}
		// Spring's own User class: the email is the "username"; we don't use roles (yet).
		return User.withUsername(account.getEmail()).password(account.getPasswordHash()).roles("USER").build();
	}

}