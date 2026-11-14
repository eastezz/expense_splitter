package io.github.eastezz.fairshare.expense;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import io.github.eastezz.fairshare.group.Group;
import io.github.eastezz.fairshare.group.GroupRepository;
import io.github.eastezz.fairshare.member.Member;
import io.github.eastezz.fairshare.member.MemberRepository;

/** Tests the /api/groups/{groupId}/expenses endpoints with the whole application running against H2. */
@SpringBootTest
@AutoConfigureMockMvc
class ExpenseControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private GroupRepository groupRepository;

	@Autowired
	private MemberRepository memberRepository;

	/** Plain SQL access, used to check that the shares are really gone from the table. */
	@Autowired
	private JdbcTemplate jdbcTemplate;

	private Long groupId;

	private Long anna;

	private Long ben;

	private Long chris;

	/** Every test gets its own fresh group with three members (ids in the order Anna < Ben < Chris). */
	@BeforeEach
	void createGroupWithMembers() {
		Group group = groupRepository.save(new Group("Trip to Berlin"));
		groupId = group.getId();
		anna = memberRepository.save(new Member("Anna", group)).getId();
		ben = memberRepository.save(new Member("Ben", group)).getId();
		chris = memberRepository.save(new Member("Chris", group)).getId();
	}

	private ResultActions postExpense(Long groupId, String json) throws Exception {
		return mockMvc.perform(post("/api/groups/" + groupId + "/expenses")
			.contentType(MediaType.APPLICATION_JSON)
			.content(json));
	}

	/** JSON for an EQUAL expense paid by the given member and shared by everyone in memberIds. */
	private String equalExpense(String description, long amountCents, Long paidBy, Long... memberIds) {
		StringBuilder shares = new StringBuilder();
		for (Long id : memberIds) {
			shares.append(shares.isEmpty() ? "" : ",").append("{\"memberId\":").append(id).append('}');
		}
		return """
				{"description":"%s","amountCents":%d,"paidByMemberId":%d,"splitType":"EQUAL","shares":[%s]}
				""".formatted(description, amountCents, paidBy, shares);
	}

	@Test
	void equalSplitReturns201WithRemainderOnSmallestId() throws Exception {
		postExpense(groupId, equalExpense("Dinner", 1000, anna, chris, ben, anna))
			.andExpect(status().isCreated())
			.andExpect(header().string("Location", matchesPattern("/api/groups/" + groupId + "/expenses/\\d+")))
			.andExpect(jsonPath("$.id").isNumber())
			.andExpect(jsonPath("$.groupId").value(groupId))
			.andExpect(jsonPath("$.description").value("Dinner"))
			.andExpect(jsonPath("$.amountCents").value(1000))
			.andExpect(jsonPath("$.paidByMemberId").value(anna))
			.andExpect(jsonPath("$.splitType").value("EQUAL"))
			.andExpect(jsonPath("$.createdAt").isNotEmpty())
			.andExpect(jsonPath("$.shares.length()").value(3))
			.andExpect(jsonPath("$.shares[0].memberId").value(anna))
			.andExpect(jsonPath("$.shares[0].memberName").value("Anna"))
			.andExpect(jsonPath("$.shares[0].shareCents").value(334))
			.andExpect(jsonPath("$.shares[1].shareCents").value(333))
			.andExpect(jsonPath("$.shares[2].memberName").value("Chris"))
			.andExpect(jsonPath("$.shares[2].shareCents").value(333));
	}

	@Test
	void exactSplitReturns201() throws Exception {
		String json = """
				{"description":"Taxi","amountCents":1000,"paidByMemberId":%d,"splitType":"EXACT",
				 "shares":[{"memberId":%d,"amountCents":700},{"memberId":%d,"amountCents":300}]}
				""".formatted(ben, anna, ben);

		postExpense(groupId, json)
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.splitType").value("EXACT"))
			.andExpect(jsonPath("$.shares[0].memberName").value("Anna"))
			.andExpect(jsonPath("$.shares[0].shareCents").value(700))
			.andExpect(jsonPath("$.shares[1].memberName").value("Ben"))
			.andExpect(jsonPath("$.shares[1].shareCents").value(300));
	}

	@Test
	void exactSplitWithWrongSumReturns400() throws Exception {
		String json = """
				{"description":"Taxi","amountCents":1000,"paidByMemberId":%d,"splitType":"EXACT",
				 "shares":[{"memberId":%d,"amountCents":400},{"memberId":%d,"amountCents":500}]}
				""".formatted(anna, anna, ben);

		postExpense(groupId, json)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.title").value("Invalid expense"))
			.andExpect(jsonPath("$.detail").value("Shares add up to 900 cents but the total is 1000"));
	}

	@Test
	void payerFromAnotherGroupReturns400() throws Exception {
		Group otherGroup = groupRepository.save(new Group("Flat 3B"));
		Long stranger = memberRepository.save(new Member("Dora", otherGroup)).getId();

		postExpense(groupId, equalExpense("Dinner", 1000, stranger, anna, ben))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.title").value("Invalid expense"))
			.andExpect(jsonPath("$.detail").value("Member " + stranger + " is not in group " + groupId));
	}

	@Test
	void unknownShareMemberReturns400() throws Exception {
		postExpense(groupId, equalExpense("Dinner", 1000, anna, anna, 999999L))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.detail").value("Member 999999 is not in group " + groupId));
	}

	@Test
	void unknownGroupReturns404() throws Exception {
		postExpense(999999L, equalExpense("Dinner", 1000, anna, anna))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.detail").value("Group 999999 not found"));
	}

	@Test
	void blankDescriptionReturns400WithFieldError() throws Exception {
		postExpense(groupId, equalExpense(" ", 1000, anna, anna))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors.description").isNotEmpty());
	}

	@Test
	void listReturnsExpensesInOrder() throws Exception {
		postExpense(groupId, equalExpense("Dinner", 1000, anna, anna, ben)).andExpect(status().isCreated());
		postExpense(groupId, equalExpense("Museum", 2400, ben, anna, ben, chris)).andExpect(status().isCreated());

		mockMvc.perform(get("/api/groups/" + groupId + "/expenses"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(2))
			.andExpect(jsonPath("$[0].description").value("Dinner"))
			.andExpect(jsonPath("$[0].shares.length()").value(2))
			.andExpect(jsonPath("$[1].description").value("Museum"))
			.andExpect(jsonPath("$[1].shares.length()").value(3))
			.andExpect(jsonPath("$[1].shares[2].shareCents").value(800));
	}

	/** Creates an equal "Dinner" expense shared by Anna and Ben and returns its id. */
	private long createDinner() throws Exception {
		String body = postExpense(groupId, equalExpense("Dinner", 1000, anna, anna, ben))
			.andExpect(status().isCreated())
			.andReturn().getResponse().getContentAsString();
		return ((Number) JsonPath.read(body, "$.id")).longValue();
	}

	@Test
	void getReturnsOneExpense() throws Exception {
		long expenseId = createDinner();

		mockMvc.perform(get("/api/groups/" + groupId + "/expenses/" + expenseId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(expenseId))
			.andExpect(jsonPath("$.description").value("Dinner"))
			.andExpect(jsonPath("$.shares.length()").value(2))
			.andExpect(jsonPath("$.shares[1].memberName").value("Ben"))
			.andExpect(jsonPath("$.shares[1].shareCents").value(500));
	}

	@Test
	void getUnknownExpenseReturns404() throws Exception {
		mockMvc.perform(get("/api/groups/" + groupId + "/expenses/999999"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.detail").value("Expense 999999 not found in group " + groupId));
	}

	@Test
	void getExpenseThroughWrongGroupReturns404() throws Exception {
		long expenseId = createDinner();
		Long otherGroupId = groupRepository.save(new Group("Flat 3B")).getId();

		mockMvc.perform(get("/api/groups/" + otherGroupId + "/expenses/" + expenseId))
			.andExpect(status().isNotFound());
	}

	@Test
	void deleteReturns204AndRemovesExpenseWithShares() throws Exception {
		long expenseId = createDinner();

		mockMvc.perform(delete("/api/groups/" + groupId + "/expenses/" + expenseId))
			.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/groups/" + groupId + "/expenses/" + expenseId))
			.andExpect(status().isNotFound());
		Integer sharesLeft = jdbcTemplate.queryForObject(
				"select count(*) from expense_shares where expense_id = ?", Integer.class, expenseId);
		assertThat(sharesLeft).isZero();
	}

}
