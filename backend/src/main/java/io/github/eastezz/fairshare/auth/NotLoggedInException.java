package io.github.eastezz.fairshare.auth;

/** Thrown when a request needs a logged-in user but there is none. GlobalExceptionHandler answers 401. */
public class NotLoggedInException extends RuntimeException {

	public NotLoggedInException() {
		super("Not logged in");
	}

}