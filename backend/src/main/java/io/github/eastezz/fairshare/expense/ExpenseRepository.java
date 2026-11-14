package io.github.eastezz.fairshare.expense;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

/** Database access for expenses. Their shares are saved and deleted together with them (cascade). */
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

	List<Expense> findByGroupIdOrderByIdAsc(Long groupId);

}
