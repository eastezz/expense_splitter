package io.github.eastezz.fairshare.auth;

import io.github.eastezz.fairshare.user.UserAccount;

/** What the API shows about an account. Never contains the password or its hash. */
public record UserResponse(Long id, String email) {

	public static UserResponse from(UserAccount account) {
		return new UserResponse(account.getId(), account.getEmail());
	}

}