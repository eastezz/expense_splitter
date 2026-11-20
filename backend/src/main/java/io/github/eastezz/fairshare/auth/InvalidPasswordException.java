package io.github.eastezz.fairshare.auth;

/**
 * Thrown when a password can't be used, for example because it is longer than 72 bytes
 * (BCrypt would silently ignore the rest). GlobalExceptionHandler turns it into 400 Bad Request.
 */
public class InvalidPasswordException extends RuntimeException {

	public InvalidPasswordException(String message) {
		super(message);
	}

}