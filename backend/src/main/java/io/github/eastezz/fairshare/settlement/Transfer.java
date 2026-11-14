package io.github.eastezz.fairshare.settlement;

/** One suggested payment: fromMemberId should pay amountCents to toMemberId. */
public record Transfer(Long fromMemberId, Long toMemberId, long amountCents) {
}
