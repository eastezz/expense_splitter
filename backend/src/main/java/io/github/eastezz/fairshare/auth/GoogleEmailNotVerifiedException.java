package io.github.eastezz.fairshare.auth;

/** Thrown when Google doesn't confirm that the person really owns the email address. */
public class GoogleEmailNotVerifiedException extends RuntimeException {

	public GoogleEmailNotVerifiedException() {
		super("Google has not verified this email address");
	}

}