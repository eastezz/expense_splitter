package io.github.eastezz.fairshare.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import io.github.eastezz.fairshare.user.UserAccount;
import io.github.eastezz.fairshare.user.UserAccountRepository;

/** Checks how a Google login becomes one of our accounts. Each test uses its own emails and Google ids. */
@SpringBootTest
class GoogleAccountServiceTest {

	@Autowired
	private GoogleAccountService googleAccountService;

	@Autowired
	private UserAccountRepository userAccountRepository;

	@Test
	void createsANewAccountWithoutPassword() {
		UserAccount account = googleAccountService.signIn("g-new", "New.Person@Gmail.com", true);

		assertThat(account.getEmail()).isEqualTo("new.person@gmail.com");
		assertThat(account.getPasswordHash()).isNull();
		assertThat(account.getGoogleId()).isEqualTo("g-new");
	}

	@Test
	void connectsAnExistingAccountWithTheSameEmail() {
		UserAccount existing = userAccountRepository.save(new UserAccount("linked@example.de", "$2a$10$" + "x".repeat(53)));

		UserAccount account = googleAccountService.signIn("g-linked", "Linked@example.de", true);

		assertThat(account.getId()).isEqualTo(existing.getId());
		assertThat(userAccountRepository.findByGoogleId("g-linked")).isPresent();
	}

	@Test
	void findsTheAccountByGoogleIdNextTime() {
		UserAccount first = googleAccountService.signIn("g-again", "again@gmail.com", true);

		// Even if the email at Google changed in the meantime, the Google id leads to the same account.
		UserAccount second = googleAccountService.signIn("g-again", "changed@gmail.com", true);

		assertThat(second.getId()).isEqualTo(first.getId());
	}

	@Test
	void refusesAnEmailThatGoogleHasNotVerified() {
		assertThatThrownBy(() -> googleAccountService.signIn("g-unverified", "someone@example.de", false))
			.isInstanceOf(GoogleEmailNotVerifiedException.class);
		assertThat(userAccountRepository.findByEmail("someone@example.de")).isEmpty();
	}

}