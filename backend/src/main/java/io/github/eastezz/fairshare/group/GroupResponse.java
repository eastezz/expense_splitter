package io.github.eastezz.fairshare.group;

import java.time.Instant;

/**
 * What the API sends back for a group. Using a separate record (instead of the entity)
 * keeps the JSON shape under our control, even if the database entity changes.
 */
public record GroupResponse(Long id, String name, Instant createdAt) {

	/** Copies the fields of a stored group into a response. */
	public static GroupResponse from(Group group) {
		return new GroupResponse(group.getId(), group.getName(), group.getCreatedAt());
	}

}
