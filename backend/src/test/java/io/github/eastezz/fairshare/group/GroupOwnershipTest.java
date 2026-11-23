package io.github.eastezz.fairshare.group;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import io.github.eastezz.fairshare.TestUsers;
import io.github.eastezz.fairshare.user.UserAccountRepository;

/**
 * Checks that nobody can see or change another user's group.
 * Anna owns a group; every request here is sent by Ben, who must get 404 as if the group didn't exist.
 */
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = TestUsers.BEN)
class GroupOwnershipTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private GroupRepository groupRepository;

	@Autowired
	private UserAccountRepository userAccountRepository;

	private Long annasGroupId;

	@BeforeEach
	void createAnnasGroup() {
		TestUsers.ensure(userAccountRepository, TestUsers.BEN);
		annasGroupId = groupRepository
			.save(new Group("Anna's secret trip", TestUsers.ensure(userAccountRepository, TestUsers.ANNA)))
			.getId();
	}

	@Test
	void groupListOnlyShowsOwnGroups() throws Exception {
		mockMvc.perform(get("/api/groups"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[*].name", not(hasItem("Anna's secret trip"))));
	}

	/** Every way of reading another user's group answers 404, exactly like for an unknown id. */
	@ParameterizedTest
	@ValueSource(strings = { "", "/members", "/expenses", "/expenses/1", "/payments", "/balances", "/settlements" })
	void readingSomeoneElsesGroupReturns404(String path) throws Exception {
		mockMvc.perform(get("/api/groups/" + annasGroupId + path)).andExpect(status().isNotFound());
	}

	@Test
	void addingAMemberToSomeoneElsesGroupReturns404() throws Exception {
		mockMvc.perform(post("/api/groups/" + annasGroupId + "/members").with(csrf())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Ben\"}"))
			.andExpect(status().isNotFound());
	}

	@Test
	void deletingAnExpenseOfSomeoneElsesGroupReturns404() throws Exception {
		mockMvc.perform(delete("/api/groups/" + annasGroupId + "/expenses/1").with(csrf()))
			.andExpect(status().isNotFound());
	}

	@Test
	void groupWithoutOwnerIsVisibleToNobody() throws Exception {
		// Groups from before there were accounts have no owner.
		Long oldGroupId = groupRepository.save(new Group("Old group")).getId();

		mockMvc.perform(get("/api/groups/" + oldGroupId)).andExpect(status().isNotFound());
	}

}