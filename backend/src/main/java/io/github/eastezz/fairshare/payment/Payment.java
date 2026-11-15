package io.github.eastezz.fairshare.payment;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import io.github.eastezz.fairshare.group.Group;
import io.github.eastezz.fairshare.member.Member;

/**
 * Money one member really paid back to another (for example Ben pays Anna 1000 cents).
 * Unlike an expense nothing is shared: the whole amount goes from fromMember to toMember.
 */
@Entity
@Table(name = "payments")
public class Payment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "group_id")
	private Group group;

	/** The member who paid the money. */
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "from_member_id")
	private Member fromMember;

	/** The member who received the money. */
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "to_member_id")
	private Member toMember;

	@Column(nullable = false)
	private long amountCents;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	/** Needed by JPA; not meant to be used in our own code. */
	protected Payment() {
	}

	/** Creates a new payment; the creation time is set to now. */
	public Payment(Group group, Member fromMember, Member toMember, long amountCents) {
		this.group = group;
		this.fromMember = fromMember;
		this.toMember = toMember;
		this.amountCents = amountCents;
		this.createdAt = Instant.now();
	}

	public Long getId() {
		return id;
	}

	public Group getGroup() {
		return group;
	}

	public Member getFromMember() {
		return fromMember;
	}

	public Member getToMember() {
		return toMember;
	}

	public long getAmountCents() {
		return amountCents;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

}
