package io.github.eastezz.fairshare.payment;

import java.time.Instant;

/** What the API sends back for a payment, with the names of both members. */
public record PaymentResponse(Long id, Long groupId, Long fromMemberId, String fromMemberName, Long toMemberId,
		String toMemberName, long amountCents, Instant createdAt) {

	/** Copies a stored payment into a response. Must be called inside a transaction (lazy members). */
	public static PaymentResponse from(Payment payment) {
		return new PaymentResponse(payment.getId(), payment.getGroup().getId(), payment.getFromMember().getId(),
				payment.getFromMember().getName(), payment.getToMember().getId(), payment.getToMember().getName(),
				payment.getAmountCents(), payment.getCreatedAt());
	}

}
