package io.github.eastezz.fairshare.member;

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

/** REST endpoints for the members of a group under /api/groups/{groupId}/members. */
@RestController
@RequestMapping("/api/groups/{groupId}/members")
public class MemberController {

	private final MemberService memberService;

	public MemberController(MemberService memberService) {
		this.memberService = memberService;
	}

	/** POST /api/groups/{groupId}/members: adds a member, answers 201 Created. */
	@PostMapping
	public ResponseEntity<MemberResponse> create(@PathVariable Long groupId,
			@Valid @RequestBody CreateMemberRequest request) {
		MemberResponse created = memberService.create(groupId, request);
		URI location = URI.create("/api/groups/" + groupId + "/members/" + created.id());
		return ResponseEntity.created(location).body(created);
	}

	/** GET /api/groups/{groupId}/members: lists the members of the group. */
	@GetMapping
	public List<MemberResponse> list(@PathVariable Long groupId) {
		return memberService.findByGroup(groupId);
	}

}
