package io.github.eastezz.fairshare.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

/** Checks that accounts are saved with a normalized email and that an email can't be used twice. */
@DataJpaTest
class UserAccountRepositoryTest {

	// Any 60-character text is fine here; real hashes come from BCrypt in a later piece.
	private static final String HASH = "$2a$10$" + "x".repeat(53);

	@Autowired
	private UserAccountRepository userAccountRepository;

	@Test
	void savesAccountWithNormalizedEmail() {
		UserAccount saved = userAccountRepository.save(new UserAccount(" Anna@Example.de ", HASH));

		assertThat(saved.getId()).isNotNull();
		assertThat(saved.getEmail()).isEqualTo("anna@example.de");
		assertThat(userAccountRepository.findByEmail("anna@example.de")).isPresent();
		assertThat(userAccountRepository.existsByEmail("anna@example.de")).isTrue();
		assertThat(userAccountRepository.existsByEmail("ben@example.de")).isFalse();
	}

	@Test
	void rejectsSecondAccountWithSameEmail() {
		userAccountRepository.saveAndFlush(new UserAccount("anna@example.de", HASH));

		// Different spelling, same normalized email: the UNIQUE constraint in the database refuses it.
		assertThatThrownBy(() -> userAccountRepository.saveAndFlush(new UserAccount("ANNA@example.de", HASH)))
			.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void savesGoogleAccountWithoutPassword() {
		userAccountRepository.saveAndFlush(UserAccount.fromGoogle("Greta@Gmail.com", "google-sub-123"));

		UserAccount found = userAccountRepository.findByGoogleId("google-sub-123").orElseThrow();
		assertThat(found.getEmail()).isEqualTo("greta@gmail.com");
		assertThat(found.getPasswordHash()).isNull();
	}

}
