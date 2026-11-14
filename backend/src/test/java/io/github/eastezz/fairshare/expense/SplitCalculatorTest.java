package io.github.eastezz.fairshare.expense;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

/** Plain unit tests for SplitCalculator: no Spring, no database, so they run in milliseconds. */
class SplitCalculatorTest {

	private final SplitCalculator calculator = new SplitCalculator();

	@Test
	void evenSplitGivesEveryoneTheSame() {
		Map<Long, Long> shares = calculator.splitEqually(900, List.of(1L, 2L, 3L));

		assertThat(shares).containsExactly(Map.entry(1L, 300L), Map.entry(2L, 300L), Map.entry(3L, 300L));
	}

	@Test
	void remainderGoesToSmallestIdsFirst() {
		assertThat(calculator.splitEqually(1000, List.of(1L, 2L, 3L)))
			.containsExactly(Map.entry(1L, 334L), Map.entry(2L, 333L), Map.entry(3L, 333L));
		assertThat(calculator.splitEqually(1, List.of(1L, 2L, 3L)))
			.containsExactly(Map.entry(1L, 1L), Map.entry(2L, 0L), Map.entry(3L, 0L));
		assertThat(calculator.splitEqually(2, List.of(1L, 2L, 3L)))
			.containsExactly(Map.entry(1L, 1L), Map.entry(2L, 1L), Map.entry(3L, 0L));
	}

	@Test
	void singleMemberPaysEverything() {
		assertThat(calculator.splitEqually(1234, List.of(7L))).containsExactly(Map.entry(7L, 1234L));
	}

	@Test
	void unsortedIdsAreSortedBeforeSplitting() {
		Map<Long, Long> shares = calculator.splitEqually(1000, List.of(30L, 10L, 20L));

		assertThat(shares).containsExactly(Map.entry(10L, 334L), Map.entry(20L, 333L), Map.entry(30L, 333L));
	}

	@Test
	void exactSplitThatAddsUpIsAccepted() {
		Map<Long, Long> shares = calculator.validateExact(1000, Map.of(2L, 700L, 1L, 300L));

		assertThat(shares).containsExactly(Map.entry(1L, 300L), Map.entry(2L, 700L));
	}

	@Test
	void exactSplitWithWrongSumIsRejected() {
		assertThatThrownBy(() -> calculator.validateExact(1000, Map.of(1L, 400L, 2L, 500L)))
			.isInstanceOf(InvalidExpenseException.class)
			.hasMessage("Shares add up to 900 cents but the total is 1000");
	}

	@Test
	void exactSplitWithNegativeAmountIsRejected() {
		assertThatThrownBy(() -> calculator.validateExact(1000, Map.of(1L, 1100L, 2L, -100L)))
			.isInstanceOf(InvalidExpenseException.class)
			.hasMessageContaining("member 2");
	}

	@Test
	void emptyMemberListIsRejected() {
		assertThatThrownBy(() -> calculator.splitEqually(1000, List.of()))
			.isInstanceOf(InvalidExpenseException.class);
		assertThatThrownBy(() -> calculator.validateExact(1000, Map.of()))
			.isInstanceOf(InvalidExpenseException.class);
	}

	@Test
	void duplicateIdsAreRejected() {
		assertThatThrownBy(() -> calculator.splitEqually(1000, List.of(1L, 2L, 1L)))
			.isInstanceOf(InvalidExpenseException.class)
			.hasMessageContaining("only once");
	}

	@Test
	void totalOfZeroOrLessIsRejected() {
		assertThatThrownBy(() -> calculator.splitEqually(0, List.of(1L)))
			.isInstanceOf(InvalidExpenseException.class);
		assertThatThrownBy(() -> calculator.validateExact(-5, Map.of(1L, -5L)))
			.isInstanceOf(InvalidExpenseException.class);
	}

}
