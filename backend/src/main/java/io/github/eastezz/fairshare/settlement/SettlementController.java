package io.github.eastezz.fairshare.settlement;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST endpoint for the suggested transfers of a group under /api/groups/{groupId}/settlements. */
@RestController
@RequestMapping("/api/groups/{groupId}")
public class SettlementController {

	private final SettlementService settlementService;

	public SettlementController(SettlementService settlementService) {
		this.settlementService = settlementService;
	}

	/** GET /api/groups/{groupId}/settlements: who should pay whom so that everyone ends at 0. */
	@GetMapping("/settlements")
	public List<TransferResponse> settlements(@PathVariable Long groupId) {
		return settlementService.suggestTransfers(groupId);
	}

}
