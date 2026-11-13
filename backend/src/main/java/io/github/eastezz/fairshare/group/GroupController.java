package io.github.eastezz.fairshare.group;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** REST endpoints for groups under /api/groups. */
@RestController
@RequestMapping("/api/groups")
public class GroupController {

	private final GroupService groupService;

	public GroupController(GroupService groupService) {
		this.groupService = groupService;
	}

	/** POST /api/groups: creates a group, answers 201 Created with a Location header. */
	@PostMapping
	public ResponseEntity<GroupResponse> create(@Valid @RequestBody CreateGroupRequest request) {
		GroupResponse created = groupService.create(request);
		return ResponseEntity.created(URI.create("/api/groups/" + created.id())).body(created);
	}

	/** GET /api/groups: lists all groups. */
	@GetMapping
	public List<GroupResponse> list() {
		return groupService.findAll();
	}

	/** GET /api/groups/{id}: returns one group. */
	@GetMapping("/{id}")
	public GroupResponse get(@PathVariable Long id) {
		return groupService.findById(id);
	}

}
