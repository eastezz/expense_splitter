package io.github.eastezz.fairshare.expense;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import io.github.eastezz.fairshare.group.Group;
import io.github.eastezz.fairshare.member.Member;

/**
 * Something one member paid for the group (for example "Dinner", 1000 cents, paid by Anna).
 * The shares say how much of it each member owes. Money is always whole cents.
 */
@Entity
@Table(name = "expenses")
public class Expense {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "group_id")
	private Group group;

	@Column(nullable = false, length = 100)
	private String description;

	@Column(nullable = false)
	private long amountCents;

	/** The member who paid the whole amount. */
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "paid_by_member_id")
	private Member paidBy;

	/** Stored as the text "EQUAL" or "EXACT" (safer than a number if the enum changes). */
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private SplitType splitType;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	/**
	 * The shares belong to this expense: cascade ALL saves and deletes them together with it,
	 * and orphanRemoval deletes a share that is removed from this list.
	 */
	@OneToMany(mappedBy = "expense", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("id ASC")
	private List<ExpenseShare> shares = new ArrayList<>();

	/** Needed by JPA; not meant to be used in our own code. */
	protected Expense() {
	}

	/** Creates a new expense without shares; add them with addShare. The creation time is set to now. */
	public Expense(Group group, String description, long amountCents, Member paidBy, SplitType splitType) {
		this.group = group;
		this.description = description;
		this.amountCents = amountCents;
		this.paidBy = paidBy;
		this.splitType = splitType;
		this.createdAt = Instant.now();
	}

	/** Adds the share of one member and links it back to this expense (both sides of the relation). */
	public void addShare(Member member, long shareCents) {
		shares.add(new ExpenseShare(this, member, shareCents));
	}

	public Long getId() {
		return id;
	}

	public Group getGroup() {
		return group;
	}

	public String getDescription() {
		return description;
	}

	public long getAmountCents() {
		return amountCents;
	}

	public Member getPaidBy() {
		return paidBy;
	}

	public SplitType getSplitType() {
		return splitType;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	/** The shares as a read-only list; use addShare to change them. */
	public List<ExpenseShare> getShares() {
		return List.copyOf(shares);
	}

}
