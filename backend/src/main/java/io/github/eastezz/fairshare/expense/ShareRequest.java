package io.github.eastezz.fairshare.expense;

import jakarta.validation.constraints.NotNull;

/**
 * One member taking part in an expense. amountCents is required for an EXACT split
 * and must be left out (null) for an EQUAL split, where it is calculated.
 */
public record ShareRequest(@NotNull Long memberId, Long amountCents) {
}
