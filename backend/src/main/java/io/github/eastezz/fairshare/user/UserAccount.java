package io.github.eastezz.fairshare.user;

import java.time.Instant;
import java.util.Locale;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * A registered person who can log in (stored in the table "users").
 * Called UserAccount, not User, so it is not mixed up with Spring Security's own User class.
 * The password is never stored, only its BCrypt hash. An account that was created with
 * "Sign in with Google" has no password of its own (passwordHash is null) but a googleId.
 */
@Entity
@Table(name = "users")
public class UserAccount {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 254)
	private String email;

	/** BCrypt hash of the password, or null for an account that only signs in with Google. */
	@Column(length = 60)
	private String passwordHash;

	/** Google's permanent id for this person (the "sub" claim), or null if Google was never used. */
	@Column(unique = true)
	private String googleId;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	/** Needed by JPA; not meant to be used in our own code. */
	protected UserAccount() {
	}

	/** Creates a new account; the email is normalized and the creation time is set to now. */
	public UserAccount(String email, String passwordHash) {
		this.email = normalizeEmail(email);
		this.passwordHash = passwordHash;
		this.createdAt = Instant.now();
	}

	/** Creates an account for someone who signed in with Google: no password, but the Google id. */
	public static UserAccount fromGoogle(String email, String googleId) {
		UserAccount account = new UserAccount(email, null);
		account.googleId = googleId;
		return account;
	}

	/** Connects an existing account with a Google account, so both ways of logging in lead here. */
	public void linkGoogle(String googleId) {
		this.googleId = googleId;
	}

	/**
	 * The form in which emails are saved and searched: without spaces around it and in lower case,
	 * so " Anna@Example.de" and "anna@example.de" are the same account.
	 * Locale.ROOT makes lower-casing work the same on every computer (in Turkish, "I" would become "ı").
	 */
	public static String normalizeEmail(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}

	public Long getId() {
		return id;
	}

	public String getEmail() {
		return email;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public String getGoogleId() {
		return googleId;
	}

}