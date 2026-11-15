package io.github.eastezz.fairshare.payment;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import io.github.eastezz.fairshare.group.Group;
import io.github.eastezz.fairshare.group.GroupRepository;
import io.github.eastezz.fairshare.member.Member;
import io.github.eastezz.fairshare.member.MemberRepository;

/** Tests the /api/groups/{groupId}/payments endpoints with the whole application running against H2. */
@SpringBootTest
@AutoConfigureMockMvc
class PaymentControllerTest {

	@Autowired
	private MockMvc mockMvc;

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
		Group group = groupRepository.save(new Group("Trip to Berlin"));
		groupId = group.getId();
		anna = memberRepository.save(new Member("Anna", group)).getId();
		ben = memberRepository.save(new Member("Ben", group)).getId();
		cem = memberRepository.save(new Member("Cem", group)).getId();
	}

	private ResultActions postPayment(Long groupId, Long from, Long to, long amountCents) throws Exception {
		String json = """
				{"fromMemberId":%d,"toMemberId":%d,"amountCents":%d}
				""".formatted(from, to, amountCents);
		return mockMvc.perform(post("/api/groups/" + groupId + "/payments")
			.contentType(MediaType.APPLICATION_JSON)
			.content(json));
	}

	/** Adds an EQUAL expense shared by all three members through the real POST endpoint. */
	private void postEqualExpenseForAll(String description, long amountCents, Long paidBy) throws Exception {
		String json = """
				{"description":"%s","amountCents":%d,"paidByMemberId":%d,"splitType":"EQUAL",
				 "shares":[{"memberId":%d},{"memberId":%d},{"memberId":%d}]}
				""".formatted(description, amountCents, paidBy, anna, ben, cem);
		mockMvc.perform(post("/api/groups/" + groupId + "/expenses")
			.contentType(MediaType.APPLICATION_JSON)
			.content(json)).andExpect(status().isCreated());
	}

	@Test
	void createPaymentReturns201WithNames() throws Exception {
		postPayment(groupId, ben, anna, 1000)
			.andExpect(status().isCreated())
			.andExpect(header().string("Location", matchesPattern("/api/groups/" + groupId + "/payments/\\d+")))
			.andExpect(jsonPath("$.id").isNumber())
			.andExpect(jsonPath("$.groupId").value(groupId))
			.andExpect(jsonPath("$.fromMemberId").value(ben))
			.andExpect(jsonPath("$.fromMemberName").value("Ben"))
			.andExpect(jsonPath("$.toMemberId").value(anna))
			.andExpect(jsonPath("$.toMemberName").value("Anna"))
			.andExpect(jsonPath("$.amountCents").value(1000))
			.andExpect(jsonPath("$.createdAt").isNotEmpty());
	}

	@Test
	void listReturnsPaymentsInOrder() throws Exception {
		postPayment(groupId, ben, anna, 1000).andExpect(status().isCreated());
		postPayment(groupId, cem, anna, 4000).andExpect(status().isCreated());

		mockMvc.perform(get("/api/groups/" + groupId + "/payments"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(2))
			.andExpect(jsonPath("$[0].fromMemberName").value("Ben"))
			.andExpect(jsonPath("$[1].fromMemberName").value("Cem"))
			.andExpect(jsonPath("$[1].amountCents").value(4000));
	}

	@Test
	void payingYourselfReturns400() throws Exception {
		postPayment(groupId, anna, anna, 1000)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.title").value("Invalid payment"))
			.andExpect(jsonPath("$.detail").value("A member cannot pay themselves"));
	}

	@Test
	void memberOfAnotherGroupReturns400() throws Exception {
		Group other = groupRepository.save(new Group("Other group"));
		Long stranger = memberRepository.save(new Member("Dora", other)).getId();

		postPayment(groupId, stranger, anna, 1000)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.title").value("Invalid payment"))
			.andExpect(jsonPath("$.detail").value("Member " + stranger + " is not in group " + groupId));
	}

	@Test
	void unknownGroupReturns404() throws Exception {
		postPayment(999999L, ben, anna, 1000)
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.detail").value("Group 999999 not found"));
		mockMvc.perform(get("/api/groups/999999/payments")).andExpect(status().isNotFound());
	}

	@Test
	void amountOfZeroReturns400() throws Exception {
		postPayment(groupId, ben, anna, 0)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors.amountCents").exists());
	}

	@Test
	void missingMemberReturns400() throws Exception {
		mockMvc.perform(post("/api/groups/" + groupId + "/payments")
			.contentType(MediaType.APPLICATION_JSON)
			.content("{\"toMemberId\":%d,\"amountCents\":100}".formatted(anna)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors.fromMemberId").exists());
	}

	@Test
	void payingEverySuggestedTransferSettlesTheGroup() throws Exception {
		postEqualExpenseForAll("Hotel", 9000, anna);
		postEqualExpenseForAll("Pizza", 3000, ben);

		String json = mockMvc.perform(get("/api/groups/" + groupId + "/settlements"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(2))
			.andReturn()
			.getResponse()
			.getContentAsString();

		List<Map<String, Object>> transfers = JsonPath.read(json, "$");
		for (Map<String, Object> transfer : transfers) {
			long from = ((Number) transfer.get("fromMemberId")).longValue();
			long to = ((Number) transfer.get("toMemberId")).longValue();
			long amount = ((Number) transfer.get("amountCents")).longValue();
			postPayment(groupId, from, to, amount).andExpect(status().isCreated());
		}

		mockMvc.perform(get("/api/groups/" + groupId + "/balances"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].balanceCents").value(0))
			.andExpect(jsonPath("$[1].balanceCents").value(0))
			.andExpect(jsonPath("$[2].balanceCents").value(0));
		mockMvc.perform(get("/api/groups/" + groupId + "/settlements"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.length()").value(0));
	}

}
