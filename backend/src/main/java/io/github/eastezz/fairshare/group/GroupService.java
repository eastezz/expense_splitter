package io.github.eastezz.fairshare.group;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business logic for groups. The controller only handles HTTP; this class talks to the
 * repository and runs each method inside a database transaction.
 */
@Service
@Transactional
public class GroupService {

	private final GroupRepository groupRepository;

	public GroupService(GroupRepository groupRepository) {
		this.groupRepository = groupRepository;
	}

	/** Saves a new group and returns it with its generated id. */
	public GroupResponse create(CreateGroupRequest request) {
		Group saved = groupRepository.save(new Group(request.name().trim()));
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
