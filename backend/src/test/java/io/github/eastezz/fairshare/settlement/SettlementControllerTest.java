package io.github.eastezz.fairshare.settlement;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

import io.github.eastezz.fairshare.group.Group;
import io.github.eastezz.fairshare.group.GroupRepository;
import io.github.eastezz.fairshare.member.Member;
import io.github.eastezz.fairshare.member.MemberRepository;
import io.github.eastezz.fairshare.TestUsers;
import io.github.eastezz.fairshare.user.UserAccount;
import io.github.eastezz.fairshare.user.UserAccountRepository;

/** Tests GET /api/groups/{groupId}/settlements with the whole application running against H2. */
@SpringBootTest
// Every request in this class runs as a logged-in user (the API needs a login).
@WithMockUser(username = "anna@example.de")
@AutoConfigureMockMvc
class SettlementControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserAccountRepository userAccountRepository;

	/** The logged-in test user's account (see @WithMockUser); the test groups belong to it. */
	private UserAccount anna() {
		return TestUsers.ensure(userAccountRepository, TestUsers.ANNA);
	}

	@Autowired
	private GroupRepository groupRepository;

	@Autowired
	private MemberRepository memberRepository;

	private Long groupId;

	private Long anna;

	private Long ben;

	private Long cem;

	/** Every test gets its own fresh group with three members (ids in the order Anna < Ben < Cem). */
	@BeforeEach
	void createGroupWithMembers() {
		Group group = groupRepository.save(new Group("Trip to Berlin", anna()));
		groupId = group.getId();
		anna = memberRepository.save(new Member("Anna", group)).getId();
		ben = memberRepository.save(new Member("Ben", group)).getId();
		cem = memberRepository.save(new Member("Cem", group)).getId();
	}

	/** Adds an EQUAL expense shared by all three members through the real POST endpoint. */
	private void postEqualExpenseForAll(String description, long amountCents, Long paidBy) throws Exception {
		String json = """
				{"description":"%s","amountCents":%d,"paidByMemberId":%d,"splitType":"EQUAL",
				 "shares":[{"memberId":%d},{"memberId":%d},{"memberId":%d}]}
				""".formatted(description, amountCents, paidBy, anna, ben, cem);
		mockMvc.perform(post("/api/groups/" + groupId + "/expenses").with(csrf())
			.contentType(MediaType.APPLICATION_JSON)
			.content(json)).andExpect(status().isCreated());
	}

	@Test
	void planExampleGivesTwoTransfersWithNames() throws Exception {
		postEqualExpenseForAll("Hotel", 9000, anna);
		postEqualExpenseForAll("Pizza", 3000, ben);

		mockMvc.perform(get("/api/groups/" + groupId + "/settlements"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(2))
			.andExpect(jsonPath("$[0].fromMemberId").value(cem))
			.andExpect(jsonPath("$[0].fromMemberName").value("Cem"))
			.andExpect(jsonPath("$[0].toMemberId").value(anna))
			.andExpect(jsonPath("$[0].toMemberName").value("Anna"))
			.andExpect(jsonPath("$[0].amountCents").value(4000))
			.andExpect(jsonPath("$[1].fromMemberName").value("Ben"))
			.andExpect(jsonPath("$[1].fromMemberId").value(ben))
			.andExpect(jsonPath("$[1].toMemberName").value("Anna"))
			.andExpect(jsonPath("$[1].amountCents").value(1000));
	}

	@Test
	void groupWithoutExpensesNeedsNoTransfers() throws Exception {
		mockMvc.perform(get("/api/groups/" + groupId + "/settlements"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(0));
	}

	@Test
	void unknownGroupReturns404() throws Exception {
		mockMvc.perform(get("/api/groups/999999/settlements"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.detail").value("Group 999999 not found"));
	}

}
