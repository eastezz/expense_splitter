package io.github.eastezz.fairshare.auth;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import io.github.eastezz.fairshare.user.UserAccount;
import io.github.eastezz.fairshare.user.UserAccountRepository;

/**
 * Tests register, login, "me" and logout under /api/auth with the whole application running against H2.
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
		return mockMvc.perform(post("/api/auth/register").with(csrf())
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"));
	}

	private ResultActions login(String email, String password) throws Exception {
		return mockMvc.perform(post("/api/auth/login").with(csrf())
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"));
	}

	/** Logs in and returns the session, like a browser that keeps the JSESSIONID cookie. */
	private MockHttpSession loginAndGetSession(String email, String password) throws Exception {
		MvcResult result = login(email, password).andExpect(status().isOk()).andReturn();
		return (MockHttpSession) result.getRequest().getSession(false);
	}

	@Test
	void loginWithRightPasswordStartsASession() throws Exception {
		register("eva@example.de", "secret123").andExpect(status().isCreated());

		// Other letters in the email still log in to the same account.
		MockHttpSession session = loginAndGetSession("EVA@example.de", "secret123");

		mockMvc.perform(get("/api/auth/me").session(session))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.email").value("eva@example.de"));
	}

	@Test
	void loginWithWrongPasswordReturns401() throws Exception {
		register("felix@example.de", "secret123").andExpect(status().isCreated());

		login("felix@example.de", "wrong-password")
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.detail").value("Wrong email or password"));
	}

	@Test
	void loginWithUnknownEmailGivesTheSameAnswer() throws Exception {
		// Same message as a wrong password, so nobody can find out which emails have an account.
		login("nobody@example.de", "secret123")
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.detail").value("Wrong email or password"));
	}

	@Test
	void meWithoutLoginReturns401() throws Exception {
		mockMvc.perform(get("/api/auth/me"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.detail").value("Not logged in"));
	}

	@Test
	void logoutEndsTheSession() throws Exception {
		register("greta@example.de", "secret123").andExpect(status().isCreated());
		MockHttpSession session = loginAndGetSession("greta@example.de", "secret123");

		mockMvc.perform(post("/api/auth/logout").session(session).with(csrf())).andExpect(status().isNoContent());

		assertThat(session.isInvalid()).isTrue();
		mockMvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
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
