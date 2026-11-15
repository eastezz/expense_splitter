package io.github.eastezz.fairshare.settlement;

/** What the API sends back for one suggested transfer: who pays whom how many cents, with names. */
public record TransferResponse(Long fromMemberId, String fromMemberName, Long toMemberId, String toMemberName,
		long amountCents) {
}
