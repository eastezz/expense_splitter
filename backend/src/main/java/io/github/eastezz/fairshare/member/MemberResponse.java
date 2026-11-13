package io.github.eastezz.fairshare.member;

/** What the API sends back for a member: its id, name and the id of its group. */
public record MemberResponse(Long id, String name, Long groupId) {

	/** Copies the fields of a stored member into a response. */
	public static MemberResponse from(Member member) {
		return new MemberResponse(member.getId(), member.getName(), member.getGroup().getId());
	}

}
