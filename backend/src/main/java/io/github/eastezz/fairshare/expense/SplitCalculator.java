package io.github.eastezz.fairshare.expense;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

/**
 * Works out how much each member owes for one expense. All money is in whole cents
 * (a long), never double, so no cent is ever lost to rounding.
 * Pure Java with no database, so it is easy to unit test.
 */
@Component
public class SplitCalculator {

	/**
	 * Splits the total equally. Everyone gets totalCents / n; the leftover cents
	 * (totalCents % n) go one each to the members with the smallest ids.
	 * Example: 1000 cents over 3 members gives 334, 333, 333.
	 *
	 * @return member id -> share in cents, ordered by member id
	 */
	public Map<Long, Long> splitEqually(long totalCents, List<Long> memberIds) {
		checkTotal(totalCents);
		checkIds(memberIds);
		List<Long> sorted = new ArrayList<>(memberIds);
		sorted.sort(null);
		int n = sorted.size();
		long base = totalCents / n;
		long remainder = totalCents % n;
		Map<Long, Long> shares = new LinkedHashMap<>();
		for (int i = 0; i < n; i++) {
			shares.put(sorted.get(i), i < remainder ? base + 1 : base);
		}
		return shares;
	}

	/**
	 * Checks shares that were given exactly: none may be negative and they must add up
	 * to the total.
	 *
	 * @return the same shares, ordered by member id
	 */
	public Map<Long, Long> validateExact(long totalCents, Map<Long, Long> amounts) {
		checkTotal(totalCents);
		if (amounts == null || amounts.isEmpty()) {
			throw new InvalidExpenseException("An expense needs at least one member to share it");
		}
		long sum = 0;
		for (Map.Entry<Long, Long> entry : amounts.entrySet()) {
			Long cents = entry.getValue();
			if (cents == null || cents < 0) {
				throw new InvalidExpenseException(
						"Share of member " + entry.getKey() + " must be 0 or more cents, but was " + cents);
			}
			sum += cents;
		}
		if (sum != totalCents) {
			throw new InvalidExpenseException(
					"Shares add up to " + sum + " cents but the total is " + totalCents);
		}
		List<Long> sortedIds = new ArrayList<>(amounts.keySet());
		sortedIds.sort(null);
		Map<Long, Long> shares = new LinkedHashMap<>();
		for (Long id : sortedIds) {
			shares.put(id, amounts.get(id));
		}
		return shares;
	}

	private static void checkTotal(long totalCents) {
		if (totalCents <= 0) {
			throw new InvalidExpenseException("The total must be more than 0 cents, but was " + totalCents);
		}
	}

	private static void checkIds(Collection<Long> memberIds) {
		if (memberIds == null || memberIds.isEmpty()) {
			throw new InvalidExpenseException("An expense needs at least one member to share it");
		}
		if (new HashSet<>(memberIds).size() != memberIds.size()) {
			throw new InvalidExpenseException("Each member may appear only once in the shares");
		}
	}

}
