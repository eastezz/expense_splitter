package io.github.eastezz.fairshare.payment;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * JSON body for POST /api/groups/{groupId}/payments, e.g.
 * {"fromMemberId":2,"toMemberId":1,"amountCents":1000}.
 * amountCents is in whole cents (1000 = 10.00 EUR); the maximum is 1,000,000.00 EUR.
 */
public record CreatePaymentRequest(
		@NotNull Long fromMemberId,
		@NotNull Long toMemberId,
		@Positive @Max(100_000_000) long amountCents) {
}
