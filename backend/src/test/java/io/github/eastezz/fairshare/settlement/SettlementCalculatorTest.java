package io.github.eastezz.fairshare.settlement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.jupiter.api.Test;

/** Plain unit tests for SettlementCalculator: no Spring, no database. */
class SettlementCalculatorTest {

	private static final long ANNA = 1L;
	private static final long BEN = 2L;
	private static final long CEM = 3L;

	private final SettlementCalculator calculator = new SettlementCalculator();

	@Test
	void planExampleGivesTwoTransfers() {
		Map<Long, Long> balances = Map.of(ANNA, 5000L, BEN, -1000L, CEM, -4000L);

		assertThat(calculator.suggest(balances))
			.containsExactly(new Transfer(CEM, ANNA, 4000), new Transfer(BEN, ANNA, 1000));
	}

	@Test
	void allZeroGivesNoTransfers() {
		assertThat(calculator.suggest(Map.of(ANNA, 0L, BEN, 0L))).isEmpty();
		assertThat(calculator.suggest(Map.of())).isEmpty();
	}

	@Test
	void twoPeopleGiveOneTransfer() {
		assertThat(calculator.suggest(Map.of(ANNA, -250L, BEN, 250L)))
			.containsExactly(new Transfer(ANNA, BEN, 250));
	}

	@Test
	void severalCreditorsAndDebtorsWithTiesBrokenBySmallerId() {
		// Members 3 and 4 both owe 300, so the smaller id (3) goes first.
		Map<Long, Long> balances = Map.of(1L, 600L, 2L, 400L, 3L, -300L, 4L, -300L, 5L, -400L);

		assertThat(calculator.suggest(balances)).containsExactly(
				new Transfer(5L, 1L, 400),
				new Transfer(3L, 2L, 300),
				new Transfer(4L, 1L, 200),
				new Transfer(4L, 2L, 100));
	}

	@Test
	void balancesThatDoNotSumToZeroAreRejected() {
		assertThatThrownBy(() -> calculator.suggest(Map.of(ANNA, 100L, BEN, -50L)))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("50");
	}

	@Test
	void transfersAlwaysSettleEveryoneInAtMostNMinusOneSteps() {
		Random random = new Random(42); // fixed seed, so the test always checks the same cases
		for (int round = 0; round < 500; round++) {
			int n = 2 + random.nextInt(9);
			Map<Long, Long> balances = new LinkedHashMap<>();
			long sum = 0;
			for (long id = 1; id < n; id++) {
				long cents = random.nextInt(20_001) - 10_000;
				balances.put(id, cents);
				sum += cents;
			}
			balances.put((long) n, -sum); // the last member makes the total 0

			List<Transfer> transfers = calculator.suggest(balances);

			assertThat(transfers).hasSizeLessThanOrEqualTo(n - 1);
			Map<Long, Long> after = new HashMap<>(balances);
			for (Transfer transfer : transfers) {
				assertThat(transfer.amountCents()).isPositive();
				after.merge(transfer.fromMemberId(), transfer.amountCents(), Long::sum);
				after.merge(transfer.toMemberId(), -transfer.amountCents(), Long::sum);
			}
			assertThat(after.values()).allMatch(cents -> cents == 0);
		}
	}

}
