package io.github.eastezz.fairshare.group;

/**
 * Thrown when a group id does not exist in the database.
 * GlobalExceptionHandler turns it into a 404 Not Found response.
 */
public class GroupNotFoundException extends RuntimeException {

	public GroupNotFoundException(Long id) {
		super("Group " + id + " not found");
	}

}
