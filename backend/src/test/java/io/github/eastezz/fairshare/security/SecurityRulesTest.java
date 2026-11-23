package io.github.eastezz.fairshare.security;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** Checks the rules in SecurityConfig: who may call what, and the CSRF protection. */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityRulesTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void apiWithoutLoginReturns401AsJson() throws Exception {
		mockMvc.perform(get("/api/groups"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.detail").value("Not logged in"));
	}

	@Test
	void healthCheckIsOpenToEveryone() throws Exception {
		mockMvc.perform(get("/api/health")).andExpect(status().isOk());
	}

	@Test
	void changingRequestWithoutCsrfTokenIsRefused() throws Exception {
		// Logged in, but without the CSRF token, like a request sent by another website.
		mockMvc.perform(post("/api/groups")
			.with(user("anna@example.de"))
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"name\":\"Trip\"}"))
			.andExpect(status().isForbidden());
	}

	@Test
	void changingRequestWithCsrfTokenIsAllowed() throws Exception {
		mockMvc.perform(post("/api/groups")
			.with(user("anna@example.de"))
			.with(csrf())
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"name\":\"Trip\"}"))
			.andExpect(status().isCreated());
	}

	// Not tested here: that every answer carries the XSRF-TOKEN cookie (CsrfCookieFilter).
	// The Spring Security test support replaces the cookie storage inside MockMvc,
	// so this is checked against the real running app instead.

}
