package io.github.eastezz.fairshare.expense;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * JSON body for POST /api/groups/{groupId}/expenses, e.g.
 * {"description":"Dinner","amountCents":1000,"paidByMemberId":1,"splitType":"EQUAL",
 * "shares":[{"memberId":1},{"memberId":2}]}.
 * amountCents is in whole cents (1000 = 10.00 EUR); the maximum is 1,000,000.00 EUR.
 */
public record CreateExpenseRequest(
		@NotBlank @Size(max = 100) String description,
		@Positive @Max(100_000_000) long amountCents,
		@NotNull Long paidByMemberId,
		@NotNull SplitType splitType,
		@NotEmpty List<@Valid ShareRequest> shares) {
}
