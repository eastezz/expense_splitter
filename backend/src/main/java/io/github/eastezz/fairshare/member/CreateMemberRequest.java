package io.github.eastezz.fairshare.member;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** JSON body for POST /api/groups/{groupId}/members, e.g. {"name":"Anna"}. */
public record CreateMemberRequest(@NotBlank @Size(max = 50) String name) {
}
