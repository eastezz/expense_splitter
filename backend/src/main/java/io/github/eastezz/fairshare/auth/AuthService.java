package io.github.eastezz.fairshare.auth;

import java.nio.charset.StandardCharsets;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.eastezz.fairshare.user.UserAccount;
import io.github.eastezz.fairshare.user.UserAccountRepository;

/** Creates user accounts and checks logins. Passwords are only ever stored as BCrypt hashes. */
@Service
public class AuthService {

	/** BCrypt only looks at the first 72 bytes of a password. */
	private static final int MAX_PASSWORD_BYTES = 72;

	private final UserAccountRepository userAccountRepository;

	private final PasswordEncoder passwordEncoder;

	private final AuthenticationManager authenticationManager;

	public AuthService(UserAccountRepository userAccountRepository, PasswordEncoder passwordEncoder,
			AuthenticationManager authenticationManager) {
		this.userAccountRepository = userAccountRepository;
		this.passwordEncoder = passwordEncoder;
		this.authenticationManager = authenticationManager;
	}

	/** Registers a new account. 409 if the email is taken, 400 if the password is too long. */
	public UserResponse register(RegisterRequest request) {
		String email = UserAccount.normalizeEmail(request.email());
		// 72 characters can be more than 72 bytes (an "ü" needs 2 bytes, an emoji 4).
		if (isTooLong(request.password())) {
			throw new InvalidPasswordException("The password is too long (at most 72 bytes)");
		}
		if (userAccountRepository.existsByEmail(email)) {
			throw new EmailAlreadyRegisteredException(email);
		}
		String hash = passwordEncoder.encode(request.password());
		try {
			// saveAndFlush sends the INSERT now, so a clash with the UNIQUE rule is caught right here.
			return UserResponse.from(userAccountRepository.saveAndFlush(new UserAccount(email, hash)));
		}
		catch (DataIntegrityViolationException ex) {
			// Two registrations with the same email at the same moment: the database refuses the second.
			throw new EmailAlreadyRegisteredException(email);
		}
	}

	/**
	 * Checks email and password. Returns the logged-in user for the session,
	 * or throws InvalidCredentialsException (401) without saying which of the two was wrong.
	 */
	public Authentication authenticate(LoginRequest request) {
		if (isTooLong(request.password())) {
			// No account can have such a password, and BCrypt would refuse to check it.
			throw new InvalidCredentialsException();
		}
		try {
			return authenticationManager.authenticate(UsernamePasswordAuthenticationToken
				.unauthenticated(UserAccount.normalizeEmail(request.email()), request.password()));
		}
		catch (AuthenticationException ex) {
			throw new InvalidCredentialsException();
		}
	}

	/** The account of the logged-in user, found by the email saved in the session. */
	@Transactional(readOnly = true)
	public UserResponse currentUser(String email) {
		return userAccountRepository.findByEmail(email).map(UserResponse::from).orElseThrow(NotLoggedInException::new);
	}

	private static boolean isTooLong(String password) {
		return password.getBytes(StandardCharsets.UTF_8).length > MAX_PASSWORD_BYTES;
	}

}