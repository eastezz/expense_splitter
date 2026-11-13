package io.github.eastezz.fairshare.member;

import io.github.eastezz.fairshare.group.Group;
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

/**
 * A person inside a group (for example "Anna" in "Trip to Berlin").
 * Each member belongs to exactly one group, and names are unique within a group.
 */
@Entity
@Table(name = "members", uniqueConstraints = @UniqueConstraint(columnNames = { "group_id", "name" }))
public class Member {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 50)
	private String name;

	/** Many members belong to one group. LAZY: the group is only loaded from the DB when needed. */
	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "group_id")
	private Group group;

	/** Needed by JPA; not meant to be used in our own code. */
	protected Member() {
	}

	/** Creates a new member with the given name in the given group. */
	public Member(String name, Group group) {
		this.name = name;
		this.group = group;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Group getGroup() {
		return group;
	}

}
