package io.github.eastezz.fairshare.expense;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import io.github.eastezz.fairshare.group.Group;
import io.github.eastezz.fairshare.member.Member;

/** Checks that an expense and its shares are saved together (cascade) and can be read back. */
@DataJpaTest
class ExpenseRepositoryTest {

	@Autowired
	private ExpenseRepository expenseRepository;

	@Autowired
	private TestEntityManager entityManager;

	@Test
	void savesExpenseWithSharesByCascade() {
		Group group = entityManager.persist(new Group("Trip to Berlin"));
		Member anna = entityManager.persist(new Member("Anna", group));
		Member ben = entityManager.persist(new Member("Ben", group));

		Expense expense = new Expense(group, "Dinner", 1000, anna, SplitType.EQUAL);
		expense.addShare(anna, 500);
		expense.addShare(ben, 500);
		expenseRepository.save(expense);

		// Write everything to the DB and forget the cached objects, so we really read from the DB.
		entityManager.flush();
		entityManager.clear();

		List<Expense> found = expenseRepository.findByGroupIdOrderByIdAsc(group.getId());
		assertThat(found).hasSize(1);
		Expense loaded = found.get(0);
		assertThat(loaded.getDescription()).isEqualTo("Dinner");
		assertThat(loaded.getAmountCents()).isEqualTo(1000);
		assertThat(loaded.getSplitType()).isEqualTo(SplitType.EQUAL);
		assertThat(loaded.getPaidBy().getName()).isEqualTo("Anna");
		assertThat(loaded.getCreatedAt()).isNotNull();
		assertThat(loaded.getShares())
			.extracting(share -> share.getMember().getName(), ExpenseShare::getShareCents)
			.containsExactly(tuple("Anna", 500L), tuple("Ben", 500L));
	}

}
