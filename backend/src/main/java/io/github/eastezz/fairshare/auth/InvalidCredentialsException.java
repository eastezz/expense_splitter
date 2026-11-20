package io.github.eastezz.fairshare.auth;

/**
 * Thrown when the email or the password is wrong. On purpose it does not say which of the two,
 * so nobody can find out which emails have an account. GlobalExceptionHandler answers 401.
 */
public class InvalidCredentialsException extends RuntimeException {

	public InvalidCredentialsException() {
		super("Wrong email or password");
	}

}