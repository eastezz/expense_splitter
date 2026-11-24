package io.github.eastezz.fairshare.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/** Database access for user accounts. Spring Data writes the SQL from the method names. */
public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

	/** Finds the account with this email. Pass the email through UserAccount.normalizeEmail first. */
	Optional<UserAccount> findByEmail(String email);

	/** True if an account with this email already exists (also normalized first). */
	boolean existsByEmail(String email);

	/** Finds the account that is connected to this Google account. */
	Optional<UserAccount> findByGoogleId(String googleId);

}