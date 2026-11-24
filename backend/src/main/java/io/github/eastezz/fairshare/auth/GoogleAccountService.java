package io.github.eastezz.fairshare.auth;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.eastezz.fairshare.user.UserAccount;
import io.github.eastezz.fairshare.user.UserAccountRepository;

/** Turns a successful Google login into one of our own accounts. */
@Service
@Transactional
public class GoogleAccountService {

	private final UserAccountRepository userAccountRepository;

	public GoogleAccountService(UserAccountRepository userAccountRepository) {
		this.userAccountRepository = userAccountRepository;
	}

	/**
	 * Finds or creates the account for this Google user:
	 * 1. an account that is already connected to this Google id, otherwise
	 * 2. an account with the same email, which gets connected now, otherwise
	 * 3. a new account without a password.
	 * Steps 2 and 3 need an email that Google has verified; otherwise someone could create a Google
	 * account with another person's email address and take over that person's account here.
	 */
	public UserAccount signIn(String googleId, String email, boolean emailVerified) {
		Optional<UserAccount> connected = userAccountRepository.findByGoogleId(googleId);
		if (connected.isPresent()) {
			return connected.get();
		}
		if (!emailVerified || email == null) {
			throw new GoogleEmailNotVerifiedException();
		}
		String normalized = UserAccount.normalizeEmail(email);
		Optional<UserAccount> sameEmail = userAccountRepository.findByEmail(normalized);
		if (sameEmail.isPresent()) {
			sameEmail.get().linkGoogle(googleId);
			return sameEmail.get();
		}
		return userAccountRepository.save(UserAccount.fromGoogle(normalized, googleId));
	}

}