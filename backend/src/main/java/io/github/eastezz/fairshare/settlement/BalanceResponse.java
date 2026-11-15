package io.github.eastezz.fairshare.settlement;

/**
 * What the API sends back for one member's balance. balanceCents above 0 means the group
 * owes this member money; below 0 means the member owes the group.
 */
public record BalanceResponse(Long memberId, String memberName, long balanceCents) {
}
