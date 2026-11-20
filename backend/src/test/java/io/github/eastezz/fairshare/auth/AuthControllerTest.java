package io.github.eastezz.fairshare.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import io.github.eastezz.fairshare.user.UserAccount;
import io.github.eastezz.fairshare.user.UserAccountRepository;

/**
 * Tests POST /api/auth/register with the whole application running against H2.
 * All tests share one database, so each test uses its own email address.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserAccountRepository userAccountRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	private ResultActions register(String email, String password) throws Exception {
		return mockMvc.perform(post("/api/auth/register")
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"));
	}

	@Test
	void registerReturns201AndStoresOnlyAHash() throws Exception {
		register("Anna@Example.de", "secret123")
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.id").isNumber())
			.andExpect(jsonPath("$.email").value("anna@example.de"))
			// The answer must never contain the password or its hash.
			.andExpect(jsonPath("$.password").doesNotExist())
			.andExpect(jsonPath("$.passwordHash").doesNotExist());

		UserAccount saved = userAccountRepository.findByEmail("anna@example.de").orElseThrow();
		assertThat(saved.getPasswordHash()).isNotEqualTo("secret123").startsWith("$2").hasSize(60);
		assertThat(passwordEncoder.matches("secret123", saved.getPasswordHash())).isTrue();
	}

	@Test
	void registerWithTakenEmailReturns409() throws Exception {
		register("ben@example.de", "secret123").andExpect(status().isCreated());

		// Same email in other letters: still the same account.
		register("BEN@example.de", "other-secret")
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.detail").value("An account with the email 'ben@example.de' already exists"));
	}

	@Test
	void registerWithInvalidEmailReturns400() throws Exception {
		register("not-an-email", "secret123")
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors.email").exists());
	}

	@Test
	void registerWithShortPasswordReturns400() throws Exception {
		register("cem@example.de", "short")
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors.password").exists());
	}

	@Test
	void registerWithPasswordOver72BytesReturns400() throws Exception {
		// 40 characters, but 80 bytes: each "ü" needs 2 bytes in UTF-8.
		register("dana@example.de", "ü".repeat(40))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.detail").value("The password is too long (at most 72 bytes)"));
	}

}