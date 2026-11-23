package io.github.eastezz.fairshare.group;

import org.springframework.stereotype.Component;

import io.github.eastezz.fairshare.user.CurrentUser;

/**
 * The one place that decides whether the logged-in user may use a group.
 * Every service that works with a group (members, expenses, payments, balances) asks here first.
 */
@Component
public class GroupAccess {

	private final GroupRepository groupRepository;

	private final CurrentUser currentUser;

	public GroupAccess(GroupRepository groupRepository, CurrentUser currentUser) {
		this.groupRepository = groupRepository;
		this.currentUser = currentUser;
	}

	/**
	 * The group with this id, if it belongs to the logged-in user. Otherwise GroupNotFoundException (404):
	 * someone else's group gives exactly the same answer as a group that doesn't exist,
	 * so nobody can find out which group ids are in use.
	 */
	public Group requireOwnGroup(Long groupId) {
		Long ownerId = currentUser.require().getId();
		return groupRepository.findByIdAndOwnerId(groupId, ownerId).orElseThrow(() -> new GroupNotFoundException(groupId));
	}

}