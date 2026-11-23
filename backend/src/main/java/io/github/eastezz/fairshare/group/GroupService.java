package io.github.eastezz.fairshare.group;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.eastezz.fairshare.user.CurrentUser;

/**
 * Business logic for groups. The controller only handles HTTP; this class talks to the
 * repository and runs each method inside a database transaction.
 */
@Service
@Transactional
public class GroupService {

	private final GroupRepository groupRepository;

	private final CurrentUser currentUser;

	public GroupService(GroupRepository groupRepository, CurrentUser currentUser) {
		this.groupRepository = groupRepository;
		this.currentUser = currentUser;
	}

	/** Saves a new group that belongs to the logged-in user and returns it with its generated id. */
	public GroupResponse create(CreateGroupRequest request) {
		Group saved = groupRepository.save(new Group(request.name().trim(), currentUser.require()));
		return GroupResponse.from(saved);
	}

	/** Returns the groups of the logged-in user, oldest first. */
	@Transactional(readOnly = true)
	public List<GroupResponse> findAll() {
		Long ownerId = currentUser.require().getId();
		return groupRepository.findByOwnerIdOrderByIdAsc(ownerId).stream().map(GroupResponse::from).toList();
	}

	/** Returns one group of the logged-in user; 404 if it doesn't exist or belongs to someone else. */
	@Transactional(readOnly = true)
	public GroupResponse findById(Long id) {
		Long ownerId = currentUser.require().getId();
		return groupRepository.findByIdAndOwnerId(id, ownerId)
			.map(GroupResponse::from)
			.orElseThrow(() -> new GroupNotFoundException(id));
	}

}
