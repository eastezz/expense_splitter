package io.github.eastezz.fairshare.group;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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

	/** Needed by JPA; not meant to be used in our own code. */
	protected Group() {
	}

	/** Creates a new group with the given name; the creation time is set to now. */
	public Group(String name) {
		this.name = name;
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

}
