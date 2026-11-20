package io.github.eastezz.fairshare.auth;

/**
 * Thrown when someone registers with an email that already has an account.
 * GlobalExceptionHandler turns it into a 409 Conflict response.
 */
public class EmailAlreadyRegisteredException extends RuntimeException {

	public EmailAlreadyRegisteredException(String email) {
		super("An account with the email '" + email + "' already exists");
	}

}