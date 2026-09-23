package com.anpr.accesscontrol.dto;

public record LoginResponseDto(
        String token,
        String username,
        String role
) {
}
