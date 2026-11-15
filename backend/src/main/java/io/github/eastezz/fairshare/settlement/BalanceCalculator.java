package io.github.eastezz.fairshare.settlement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

/**
 * Works out each member's balance in a group, in whole cents.
 * A balance above 0 means the group owes this member money; below 0 means the member
 * owes the group. Every cent paid is also a cent someone owes, so the balances of a
 * group always add up to 0.
 * Pure Java with no database, so it is easy to unit test.
 */
@Component
public class BalanceCalculator {

	/**
	 * Every member starts at 0. For each expense the payer gets +amountCents and every
	 * member who shares it gets -shareCents. For each payment the member who paid gets
	 * +amountCents (they paid back some of what they owe) and the member who received it
	 * gets -amountCents (the group owes them that much less).
	 * Members without any expenses or payments stay at 0.
	 *
	 * @return member id -> balance in cents, ordered by member id
	 * @throws IllegalArgumentException if an expense or payment mentions a member who is not in memberIds
	 */
	public Map<Long, Long> calculate(Collection<Long> memberIds, List<ExpenseEntry> expenses,
			List<PaymentEntry> payments) {
		List<Long> sorted = new ArrayList<>(memberIds);
		sorted.sort(null);
		Map<Long, Long> balances = new LinkedHashMap<>();
		for (Long id : sorted) {
			balances.put(id, 0L);
		}
		for (ExpenseEntry expense : expenses) {
			add(balances, expense.paidByMemberId(), expense.amountCents());
			for (Map.Entry<Long, Long> share : expense.sharesByMemberId().entrySet()) {
				add(balances, share.getKey(), -share.getValue());
			}
		}
		for (PaymentEntry payment : payments) {
			add(balances, payment.fromMemberId(), payment.amountCents());
			add(balances, payment.toMemberId(), -payment.amountCents());
		}
		return balances;
	}

	private static void add(Map<Long, Long> balances, Long memberId, long cents) {
		Long current = balances.get(memberId);
		if (current == null) {
			throw new IllegalArgumentException("Member " + memberId + " is not in this group");
		}
		balances.put(memberId, current + cents);
	}

}
