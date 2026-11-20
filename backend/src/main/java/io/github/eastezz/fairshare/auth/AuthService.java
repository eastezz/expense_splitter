package io.github.eastezz.fairshare.auth;

import java.nio.charset.StandardCharsets;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import io.github.eastezz.fairshare.user.UserAccount;
import io.github.eastezz.fairshare.user.UserAccountRepository;

/** Creates user accounts. The password is hashed with BCrypt before it is saved. */
@Service
public class AuthService {

	/** BCrypt only looks at the first 72 bytes of a password. */
	private static final int MAX_PASSWORD_BYTES = 72;

	private final UserAccountRepository userAccountRepository;

	private final PasswordEncoder passwordEncoder;

	public AuthService(UserAccountRepository userAccountRepository, PasswordEncoder passwordEncoder) {
		this.userAccountRepository = userAccountRepository;
		this.passwordEncoder = passwordEncoder;
	}

	/** Registers a new account. 409 if the email is taken, 400 if the password is too long. */
	public UserResponse register(RegisterRequest request) {
		String email = UserAccount.normalizeEmail(request.email());
		// 72 characters can be more than 72 bytes (an "ü" needs 2 bytes, an emoji 4).
		if (request.password().getBytes(StandardCharsets.UTF_8).length > MAX_PASSWORD_BYTES) {
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

}