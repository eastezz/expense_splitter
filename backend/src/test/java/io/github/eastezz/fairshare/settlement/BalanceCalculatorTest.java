package io.github.eastezz.fairshare.settlement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

/** Plain unit tests for BalanceCalculator: no Spring, no database. */
class BalanceCalculatorTest {

	private static final long ANNA = 1L;
	private static final long BEN = 2L;
	private static final long CEM = 3L;

	private final BalanceCalculator calculator = new BalanceCalculator();

	@Test
	void planExampleGivesExpectedBalances() {
		// Anna pays 90.00 for all three (30.00 each), Ben pays 30.00 for all three (10.00 each).
		List<ExpenseEntry> expenses = List.of(
				new ExpenseEntry(ANNA, 9000, Map.of(ANNA, 3000L, BEN, 3000L, CEM, 3000L)),
				new ExpenseEntry(BEN, 3000, Map.of(ANNA, 1000L, BEN, 1000L, CEM, 1000L)));

		Map<Long, Long> balances = calculator.calculate(List.of(ANNA, BEN, CEM), expenses, List.of());

		assertThat(balances).containsExactly(Map.entry(ANNA, 5000L), Map.entry(BEN, -1000L), Map.entry(CEM, -4000L));
	}

	@Test
	void payerWhoIsNotAmongTheSharersGetsTheFullAmount() {
		// Anna pays 20.00 for Ben and Cem only.
		List<ExpenseEntry> expenses = List.of(new ExpenseEntry(ANNA, 2000, Map.of(BEN, 1000L, CEM, 1000L)));

		Map<Long, Long> balances = calculator.calculate(List.of(ANNA, BEN, CEM), expenses, List.of());

		assertThat(balances).containsExactly(Map.entry(ANNA, 2000L), Map.entry(BEN, -1000L), Map.entry(CEM, -1000L));
	}

	@Test
	void memberWithoutExpensesStaysAtZero() {
		List<ExpenseEntry> expenses = List.of(new ExpenseEntry(ANNA, 500, Map.of(ANNA, 250L, BEN, 250L)));

		Map<Long, Long> balances = calculator.calculate(List.of(ANNA, BEN, CEM), expenses, List.of());

		assertThat(balances).containsEntry(CEM, 0L);
	}

	@Test
	void noExpensesMeansEveryoneIsAtZeroInIdOrder() {
		Map<Long, Long> balances = calculator.calculate(List.of(CEM, ANNA, BEN), List.of(), List.of());

		assertThat(balances).containsExactly(Map.entry(ANNA, 0L), Map.entry(BEN, 0L), Map.entry(CEM, 0L));
	}

	@Test
	void balancesAlwaysSumToZero() {
		List<ExpenseEntry> expenses = List.of(
				new ExpenseEntry(ANNA, 1000, Map.of(ANNA, 334L, BEN, 333L, CEM, 333L)),
				new ExpenseEntry(CEM, 777, Map.of(BEN, 777L)),
				new ExpenseEntry(BEN, 12345, Map.of(ANNA, 5000L, CEM, 7345L)),
				new ExpenseEntry(ANNA, 1, Map.of(CEM, 1L)));

		Map<Long, Long> balances = calculator.calculate(List.of(ANNA, BEN, CEM), expenses, List.of());

		assertThat(balances.values().stream().mapToLong(Long::longValue).sum()).isZero();
	}

	@Test
	void expenseWithUnknownMemberIsRejected() {
		List<ExpenseEntry> expenses = List.of(new ExpenseEntry(99L, 100, Map.of(ANNA, 100L)));

		assertThatThrownBy(() -> calculator.calculate(List.of(ANNA, BEN), expenses, List.of()))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("99");
	}

	@Test
	void paymentMovesBothBalancesTowardsZero() {
		List<ExpenseEntry> expenses = List.of(
				new ExpenseEntry(ANNA, 9000, Map.of(ANNA, 3000L, BEN, 3000L, CEM, 3000L)),
				new ExpenseEntry(BEN, 3000, Map.of(ANNA, 1000L, BEN, 1000L, CEM, 1000L)));
		// Ben pays Anna back 10.00.
		List<PaymentEntry> payments = List.of(new PaymentEntry(BEN, ANNA, 1000));

		Map<Long, Long> balances = calculator.calculate(List.of(ANNA, BEN, CEM), expenses, payments);

		assertThat(balances).containsExactly(Map.entry(ANNA, 4000L), Map.entry(BEN, 0L), Map.entry(CEM, -4000L));
	}

	@Test
	void payingAllSuggestedTransfersSettlesEveryone() {
		List<ExpenseEntry> expenses = List.of(
				new ExpenseEntry(ANNA, 9000, Map.of(ANNA, 3000L, BEN, 3000L, CEM, 3000L)),
				new ExpenseEntry(BEN, 3000, Map.of(ANNA, 1000L, BEN, 1000L, CEM, 1000L)));
		List<PaymentEntry> payments = List.of(new PaymentEntry(CEM, ANNA, 4000), new PaymentEntry(BEN, ANNA, 1000));

		Map<Long, Long> balances = calculator.calculate(List.of(ANNA, BEN, CEM), expenses, payments);

		assertThat(balances).containsExactly(Map.entry(ANNA, 0L), Map.entry(BEN, 0L), Map.entry(CEM, 0L));
	}

	@Test
	void paymentWithUnknownMemberIsRejected() {
		List<PaymentEntry> payments = List.of(new PaymentEntry(ANNA, 99L, 100));

		assertThatThrownBy(() -> calculator.calculate(List.of(ANNA, BEN), List.of(), payments))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("99");
	}

}
