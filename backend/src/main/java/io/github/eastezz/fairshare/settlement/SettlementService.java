package io.github.eastezz.fairshare.settlement;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Suggests how a group can settle up: takes the balances from BalanceService, lets
 * SettlementCalculator find the transfers and adds the member names to them.
 */
@Service
@Transactional(readOnly = true)
public class SettlementService {

	private final BalanceService balanceService;

	private final SettlementCalculator settlementCalculator;

	public SettlementService(BalanceService balanceService, SettlementCalculator settlementCalculator) {
		this.balanceService = balanceService;
		this.settlementCalculator = settlementCalculator;
	}

	/** The suggested transfers for a group; empty when everyone is settled. 404 if the group is unknown. */
	public List<TransferResponse> suggestTransfers(Long groupId) {
		List<BalanceResponse> balances = balanceService.getBalances(groupId);
		Map<Long, Long> centsByMember = new LinkedHashMap<>();
		Map<Long, String> names = new HashMap<>();
		for (BalanceResponse balance : balances) {
			centsByMember.put(balance.memberId(), balance.balanceCents());
			names.put(balance.memberId(), balance.memberName());
		}
		return settlementCalculator.suggest(centsByMember)
			.stream()
			.map(transfer -> new TransferResponse(transfer.fromMemberId(), names.get(transfer.fromMemberId()),
					transfer.toMemberId(), names.get(transfer.toMemberId()), transfer.amountCents()))
			.toList();
	}

}
