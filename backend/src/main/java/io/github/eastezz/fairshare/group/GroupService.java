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

	/** Returns all groups. */
	@Transactional(readOnly = true)
	public List<GroupResponse> findAll() {
		return groupRepository.findAll().stream().map(GroupResponse::from).toList();
	}

	/** Returns one group, or throws GroupNotFoundException (404) if the id does not exist. */
	@Transactional(readOnly = true)
	public GroupResponse findById(Long id) {
		return groupRepository.findById(id)
			.map(GroupResponse::from)
			.orElseThrow(() -> new GroupNotFoundException(id));
	}

}
