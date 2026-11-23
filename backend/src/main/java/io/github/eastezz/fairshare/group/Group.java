package io.github.eastezz.fairshare.group;

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

import io.github.eastezz.fairshare.user.UserAccount;

/**
 * A group of people who share expenses (for example "Trip to Berlin").
 * Stored in the table "expense_groups" because GROUP is a reserved SQL word.
 */
@Entity
@Table(name = "expense_groups")
public class Group {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	/**
	 * The user account that created the group. Only this user can see and change it.
	 * Can be null for groups from before there were accounts.
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "owner_id")
	private UserAccount owner;

	/** Needed by JPA; not meant to be used in our own code. */
	protected Group() {
	}

	/** Creates a new group without an owner; the creation time is set to now. */
	public Group(String name) {
		this(name, null);
	}

	/** Creates a new group that belongs to the given user; the creation time is set to now. */
	public Group(String name, UserAccount owner) {
		this.name = name;
		this.owner = owner;
		this.createdAt = Instant.now();
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public UserAccount getOwner() {
		return owner;
	}

}
