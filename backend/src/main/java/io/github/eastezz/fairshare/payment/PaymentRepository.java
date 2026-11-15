package io.github.eastezz.fairshare.payment;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

/** Database access for payments. */
public interface PaymentRepository extends JpaRepository<Payment, Long> {

	/**
	 * The payments of a group, oldest first. The @EntityGraph loads both members in the
	 * same SQL query, so their names can be read without extra queries.
	 */
	@EntityGraph(attributePaths = { "fromMember", "toMember" })
	List<Payment> findByGroupIdOrderByIdAsc(Long groupId);

}
