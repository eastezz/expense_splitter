package io.github.eastezz.fairshare.expense;

/** What the API sends back for one share: who owes it and how many cents. */
public record ShareResponse(Long memberId, String memberName, long shareCents) {

	/** Copies the fields of a stored share into a response. */
	public static ShareResponse from(ExpenseShare share) {
		return new ShareResponse(share.getMember().getId(), share.getMember().getName(), share.getShareCents());
	}

}
