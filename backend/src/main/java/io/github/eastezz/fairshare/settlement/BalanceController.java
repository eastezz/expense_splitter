package io.github.eastezz.fairshare.settlement;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST endpoint for the balances of a group under /api/groups/{groupId}/balances. */
@RestController
@RequestMapping("/api/groups/{groupId}")
public class BalanceController {

	private final BalanceService balanceService;

	public BalanceController(BalanceService balanceService) {
		this.balanceService = balanceService;
	}

	/** GET /api/groups/{groupId}/balances: every member's balance in cents, ordered by member id. */
	@GetMapping("/balances")
	public List<BalanceResponse> balances(@PathVariable Long groupId) {
		return balanceService.getBalances(groupId);
	}

}
