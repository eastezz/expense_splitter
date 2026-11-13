package io.github.eastezz.fairshare.group;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** JSON body for POST /api/groups, e.g. {"name":"Trip to Berlin"}. */
public record CreateGroupRequest(@NotBlank @Size(max = 100) String name) {
}