package io.github.eastezz.fairshare.expense;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

/** Database access for expenses. Their shares are saved and deleted together with them (cascade). */
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

	/**
	 * The expenses of a group, oldest first. The @EntityGraph loads the shares and their
	 * members in the same SQL query, instead of one extra query per expense ("N+1 problem").
	 */
	@EntityGraph(attributePaths = { "shares", "shares.member" })
	List<Expense> findByGroupIdOrderByIdAsc(Long groupId);

	/** One expense, but only if it belongs to the given group: WHERE id = ? AND group_id = ?. */
	@EntityGraph(attributePaths = { "shares", "shares.member" })
	Optional<Expense> findByIdAndGroupId(Long id, Long groupId);

}
