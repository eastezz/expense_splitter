package io.github.eastezz.fairshare.auth;

import jakarta.validation.constraints.NotBlank;

/** JSON body for POST /api/auth/login, e.g. {"email":"anna@example.de","password":"secret123"}. */
public record LoginRequest(@NotBlank String email, @NotBlank String password) {
}