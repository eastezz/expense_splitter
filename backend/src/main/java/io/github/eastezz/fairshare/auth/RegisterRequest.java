package io.github.eastezz.fairshare.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * JSON body for POST /api/auth/register, e.g. {"email":"anna@example.de","password":"secret123"}.
 * At least 8 characters; BCrypt only uses the first 72 bytes, so longer passwords are refused.
 */
public record RegisterRequest(
		@NotBlank @Email @Size(max = 254) String email,
		@NotBlank @Size(min = 8, max = 72) String password) {
}