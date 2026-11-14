package io.github.eastezz.fairshare.expense;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import io.github.eastezz.fairshare.member.Member;

/**
 * How much one member owes for one expense (for example Ben owes 333 cents of "Dinner").
 * A member can appear only once per expense.
 */
@Entity
@Table(name = "expense_shares", uniqueConstraints = @UniqueConstraint(columnNames = { "expense_id", "member_id" }))
public class ExpenseShare {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "expense_id")
	private Expense expense;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private Member member;

	@Column(nullable = false)
	private long shareCents;

	/** Needed by JPA; not meant to be used in our own code. */
	protected ExpenseShare() {
	}

	/** Use Expense.addShare instead, so both sides of the relation stay in sync. */
	ExpenseShare(Expense expense, Member member, long shareCents) {
		this.expense = expense;
		this.member = member;
		this.shareCents = shareCents;
	}

	public Long getId() {
		return id;
	}

	public Expense getExpense() {
		return expense;
	}

	public Member getMember() {
		return member;
	}

	public long getShareCents() {
		return shareCents;
	}

}
