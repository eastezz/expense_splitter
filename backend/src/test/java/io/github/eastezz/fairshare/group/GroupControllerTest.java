package io.github.eastezz.fairshare.group;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** Tests the /api/groups endpoints with the whole application running against H2. */
@SpringBootTest
@AutoConfigureMockMvc
class GroupControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private GroupRepository groupRepository;

	@Test
	void createReturns201WithLocation() throws Exception {
		mockMvc.perform(post("/api/groups")
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
		groupRepository.save(new Group("Flat 3B"));

		mockMvc.perform(get("/api/groups"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[*].name", hasItem("Flat 3B")));
	}

	@Test
	void getReturnsGroupById() throws Exception {
		Group saved = groupRepository.save(new Group("Ski weekend"));

		mockMvc.perform(get("/api/groups/" + saved.getId()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(saved.getId()))
			.andExpect(jsonPath("$.name").value("Ski weekend"));
	}

}
