package io.github.eastezz.fairshare.member;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.eastezz.fairshare.group.Group;
import io.github.eastezz.fairshare.group.GroupNotFoundException;
import io.github.eastezz.fairshare.group.GroupRepository;

/** Business logic for members: checks that the group exists and that names are unique. */
@Service
@Transactional
public class MemberService {

	private final MemberRepository memberRepository;

	private final GroupRepository groupRepository;

	public MemberService(MemberRepository memberRepository, GroupRepository groupRepository) {
		this.memberRepository = memberRepository;
		this.groupRepository = groupRepository;
	}

	/** Adds a member to a group. 404 if the group is unknown, 409 if the name is taken. */
	public MemberResponse create(Long groupId, CreateMemberRequest request) {
		Group group = groupRepository.findById(groupId).orElseThrow(() -> new GroupNotFoundException(groupId));
		String name = request.name().trim();
		if (memberRepository.existsByGroupIdAndName(groupId, name)) {
			throw new DuplicateMemberNameException(groupId, name);
		}
		Member saved = memberRepository.save(new Member(name, group));
		return MemberResponse.from(saved);
	}

	/** Lists the members of a group in the order they were added. 404 if the group is unknown. */
	@Transactional(readOnly = true)
	public List<MemberResponse> findByGroup(Long groupId) {
		if (!groupRepository.existsById(groupId)) {
			throw new GroupNotFoundException(groupId);
		}
		return memberRepository.findByGroupIdOrderByIdAsc(groupId).stream().map(MemberResponse::from).toList();
	}

}
