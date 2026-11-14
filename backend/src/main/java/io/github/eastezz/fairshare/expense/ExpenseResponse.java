package io.github.eastezz.fairshare.expense;

import java.time.Instant;
import java.util.List;

/** What the API sends back for an expense, including every member's share. */
public record ExpenseResponse(Long id, Long groupId, String description, long amountCents, Long paidByMemberId,
		SplitType splitType, Instant createdAt, List<ShareResponse> shares) {

	/**
	 * Copies a stored expense into a response. Must be called inside a transaction,
	 * because the shares and their members are loaded lazily.
	 */
	public static ExpenseResponse from(Expense expense) {
		List<ShareResponse> shares = expense.getShares().stream().map(ShareResponse::from).toList();
		return new ExpenseResponse(expense.getId(), expense.getGroup().getId(), expense.getDescription(),
				expense.getAmountCents(), expense.getPaidBy().getId(), expense.getSplitType(), expense.getCreatedAt(),
				shares);
	}

}
