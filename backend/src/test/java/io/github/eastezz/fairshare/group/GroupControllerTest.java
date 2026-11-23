package io.github.eastezz.fairshare.group;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import io.github.eastezz.fairshare.TestUsers;
import io.github.eastezz.fairshare.user.UserAccount;
import io.github.eastezz.fairshare.user.UserAccountRepository;

/** Tests the /api/groups endpoints with the whole application running against H2. */
@SpringBootTest
// Every request in this class runs as a logged-in user (the API needs a login).
@WithMockUser(username = "anna@example.de")
@AutoConfigureMockMvc
class GroupControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private GroupRepository groupRepository;

	@Autowired
	private UserAccountRepository userAccountRepository;

	/** The logged-in test user's account (see @WithMockUser); the test groups belong to it. */
	private UserAccount anna() {
		return TestUsers.ensure(userAccountRepository, TestUsers.ANNA);
	}

	/** The logged-in test user must exist as an account, because a new group gets it as owner. */
	@BeforeEach
	void createTestUser() {
		TestUsers.ensure(userAccountRepository, TestUsers.ANNA);
	}

	@Test
	void createdGroupBelongsToTheLoggedInUser() throws Exception {
		String body = mockMvc.perform(post("/api/groups").with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Owned trip\"}"))
			.andExpect(status().isCreated())
			.andReturn().getResponse().getContentAsString();
		Long id = Long.valueOf(body.replaceAll(".*\"id\":(\\d+).*", "$1"));

		// owner is loaded lazily; its id is known without loading the account from the database.
		Group saved = groupRepository.findById(id).orElseThrow();
		Long annaId = TestUsers.ensure(userAccountRepository, TestUsers.ANNA).getId();
		assertThat(saved.getOwner().getId()).isEqualTo(annaId);
	}

	@Test
	void createReturns201WithLocation() throws Exception {
		mockMvc.perform(post("/api/groups").with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Trip to Berlin\"}"))
			.andExpect(status().isCreated())
			.andExpect(header().string("Location", matchesPattern("/api/groups/\\d+")))
			.andExpect(jsonPath("$.id").isNumber())
			.andExpect(jsonPath("$.name").value("Trip to Berlin"))
			.andExpect(jsonPath("$.createdAt").isNotEmpty());
	}

	@Test
	void listContainsSavedGroup() throws Exception {
		groupRepository.save(new Group("Flat 3B", anna()));

		mockMvc.perform(get("/api/groups"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[*].name", hasItem("Flat 3B")));
	}

	@Test
	void getReturnsGroupById() throws Exception {
		Group saved = groupRepository.save(new Group("Ski weekend", anna()));

		mockMvc.perform(get("/api/groups/" + saved.getId()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(saved.getId()))
			.andExpect(jsonPath("$.name").value("Ski weekend"));
	}

	@Test
	void unknownIdReturns404ProblemDetail() throws Exception {
		mockMvc.perform(get("/api/groups/999999"))
			.andExpect(status().isNotFound())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
			.andExpect(jsonPath("$.status").value(404))
			.andExpect(jsonPath("$.detail").value("Group 999999 not found"));
	}

	@Test
	void blankNameReturns400WithFieldError() throws Exception {
		mockMvc.perform(post("/api/groups").with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"  \"}"))
			.andExpect(status().isBadRequest())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
			.andExpect(jsonPath("$.status").value(400))
			.andExpect(jsonPath("$.errors.name").isNotEmpty());
	}

}
