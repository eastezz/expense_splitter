package io.github.eastezz.fairshare;

import io.github.eastezz.fairshare.user.UserAccount;
import io.github.eastezz.fairshare.user.UserAccountRepository;

/** Test accounts. @WithMockUser only fakes the login, so the account itself must exist in the database. */
public final class TestUsers {

	/** The user that the controller tests log in as (see @WithMockUser). */
	public static final String ANNA = "anna@example.de";

	/** A second user, to check that people can't see each other's groups. */
	public static final String BEN = "ben.other@example.de";

	/** Any 60-character text works as a hash here; these accounts never log in with a password. */
	private static final String DUMMY_HASH = "$2a$10$" + "x".repeat(53);

	private TestUsers() {
	}

	/** Returns the account with this email, and saves it first if it doesn't exist yet (tests share one database). */
	public static UserAccount ensure(UserAccountRepository repository, String email) {
		return repository.findByEmail(email).orElseGet(() -> repository.save(new UserAccount(email, DUMMY_HASH)));
	}

}