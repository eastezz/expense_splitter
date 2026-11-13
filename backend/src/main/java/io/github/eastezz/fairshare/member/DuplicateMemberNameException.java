package io.github.eastezz.fairshare.member;

/**
 * Thrown when a group already has a member with the same name.
 * GlobalExceptionHandler turns it into a 409 Conflict response.
 */
public class DuplicateMemberNameException extends RuntimeException {

	public DuplicateMemberNameException(Long groupId, String name) {
		super("Group " + groupId + " already has a member named '" + name + "'");
	}

}
