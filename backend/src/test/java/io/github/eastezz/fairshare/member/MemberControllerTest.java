package io.github.eastezz.fairshare.member;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import org.springframework.test.web.servlet.ResultActions;

import io.github.eastezz.fairshare.group.Group;
import io.github.eastezz.fairshare.group.GroupRepository;

/** Tests the /api/groups/{groupId}/members endpoints with the whole application running against H2. */
@SpringBootTest
// Every request in this class runs as a logged-in user (the API needs a login).
@WithMockUser(username = "anna@example.de")
@AutoConfigureMockMvc
class MemberControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private GroupRepository groupRepository;

	private Long groupId;

	/** Every test gets its own fresh group, so tests don't see each other's members. */
	@BeforeEach
	void createGroup() {
		groupId = groupRepository.save(new Group("Trip to Berlin")).getId();
	}

	private ResultActions addMember(Long groupId, String name) throws Exception {
		return mockMvc.perform(post("/api/groups/" + groupId + "/members").with(csrf())
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"name\":\"" + name + "\"}"));
	}

	@Test
	void createReturns201WithLocation() throws Exception {
		addMember(groupId, "Anna")
			.andExpect(status().isCreated())
			.andExpect(header().string("Location", matchesPattern("/api/groups/" + groupId + "/members/\\d+")))
			.andExpect(jsonPath("$.id").isNumber())
			.andExpect(jsonPath("$.name").value("Anna"))
			.andExpect(jsonPath("$.groupId").value(groupId));
	}

	@Test
	void listReturnsMembersInOrder() throws Exception {
		addMember(groupId, "Anna").andExpect(status().isCreated());
		addMember(groupId, "Ben").andExpect(status().isCreated());

		mockMvc.perform(get("/api/groups/" + groupId + "/members"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(2))
			.andExpect(jsonPath("$[0].name").value("Anna"))
			.andExpect(jsonPath("$[1].name").value("Ben"));
	}

	@Test
	void createInUnknownGroupReturns404() throws Exception {
		addMember(999999L, "Anna")
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.detail").value("Group 999999 not found"));
	}

	@Test
	void listOfUnknownGroupReturns404() throws Exception {
		mockMvc.perform(get("/api/groups/999999/members"))
			.andExpect(status().isNotFound());
	}

	@Test
	void duplicateNameInSameGroupReturns409() throws Exception {
		addMember(groupId, "Anna").andExpect(status().isCreated());

		addMember(groupId, "Anna")
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.status").value(409));
	}

	@Test
	void sameNameInOtherGroupIsAllowed() throws Exception {
		Long otherGroupId = groupRepository.save(new Group("Flat 3B")).getId();
		addMember(groupId, "Anna").andExpect(status().isCreated());

		addMember(otherGroupId, "Anna").andExpect(status().isCreated());
	}

	@Test
	void blankNameReturns400WithFieldError() throws Exception {
		addMember(groupId, " ")
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors.name").isNotEmpty());
	}

}
