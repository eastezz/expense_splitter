package io.github.eastezz.fairshare.settlement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.stereotype.Component;

/**
 * Suggests who should pay whom so that every balance ends at 0.
 * Uses a greedy algorithm: the member who owes the most pays the member who is owed
 * the most, as much as possible, and this repeats until everyone is at 0.
 * Every transfer brings at least one member to 0, so with n members there are at most
 * n - 1 transfers. That is good but not always the absolute minimum: finding the true
 * minimum is NP-hard, so apps like this one use a greedy approach instead.
 * Pure Java with no database, so it is easy to unit test.
 */
@Component
public class SettlementCalculator {

	/**
	 * @param balances member id -> balance in cents (above 0 = is owed, below 0 = owes);
	 *     they must add up to 0
	 * @return the transfers in the order they were found; empty when everyone is at 0
	 * @throws IllegalArgumentException if the balances do not add up to 0
	 */
	public List<Transfer> suggest(Map<Long, Long> balances) {
		long sum = 0;
		for (long cents : balances.values()) {
			sum += cents;
		}
		if (sum != 0) {
			throw new IllegalArgumentException("Balances must add up to 0, but add up to " + sum);
		}

		// A TreeMap keeps members sorted by id, so on a tie the smaller id is found first.
		Map<Long, Long> open = new TreeMap<>();
		balances.forEach((id, cents) -> {
			if (cents != 0) {
				open.put(id, cents);
			}
		});

		List<Transfer> transfers = new ArrayList<>();
		while (!open.isEmpty()) {
			Long debtor = null;
			Long creditor = null;
			for (Map.Entry<Long, Long> entry : open.entrySet()) {
				long cents = entry.getValue();
				// Strict < and > keep the first (smallest) id when two members are equal.
				if (cents < 0 && (debtor == null || cents < open.get(debtor))) {
					debtor = entry.getKey();
				}
				if (cents > 0 && (creditor == null || cents > open.get(creditor))) {
					creditor = entry.getKey();
				}
			}
			long amount = Math.min(-open.get(debtor), open.get(creditor));
			transfers.add(new Transfer(debtor, creditor, amount));
			settle(open, debtor, amount);
			settle(open, creditor, -amount);
		}
		return transfers;
	}

	/** Adds cents to a member's open balance and drops the member once they reach 0. */
	private static void settle(Map<Long, Long> open, Long memberId, long cents) {
		long updated = open.get(memberId) + cents;
		if (updated == 0) {
			open.remove(memberId);
		}
		else {
			open.put(memberId, updated);
		}
	}

}
