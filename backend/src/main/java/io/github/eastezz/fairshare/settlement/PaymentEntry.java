package io.github.eastezz.fairshare.settlement;

/**
 * One payment in the simple form BalanceCalculator needs: fromMemberId paid amountCents
 * to toMemberId. No JPA here, so tests can build these by hand.
 */
public record PaymentEntry(Long fromMemberId, Long toMemberId, long amountCents) {
}
