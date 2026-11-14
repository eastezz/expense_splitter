package io.github.eastezz.fairshare.settlement;

import java.util.Map;

/**
 * One expense in the simple form BalanceCalculator needs: who paid, how much, and how
 * much each member's share is (member id -> cents). No JPA here, so tests can build
 * these by hand.
 */
public record ExpenseEntry(Long paidByMemberId, long amountCents, Map<Long, Long> sharesByMemberId) {
}
