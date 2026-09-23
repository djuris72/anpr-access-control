package com.anpr.accesscontrol.controller;

import com.anpr.accesscontrol.dto.LoginRequestDto;
import com.anpr.accesscontrol.dto.LoginResponseDto;
import com.anpr.accesscontrol.model.Admin;
import com.anpr.accesscontrol.repository.AdminRepository;
import com.anpr.accesscontrol.security.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(AdminRepository adminRepository,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /**
     * Proverava kredencijale protiv baze (lozinka je hashovana BCrypt-om,
     * nikad se ne poredi kao plain text) i vraca JWT token ako su ispravni.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        Admin admin = adminRepository.findByUsername(request.username())
                .orElse(null);

        if (admin == null || !passwordEncoder.matches(request.password(), admin.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = jwtService.generateToken(admin.getUsername(), admin.getRole());
        return ResponseEntity.ok(new LoginResponseDto(token, admin.getUsername(), admin.getRole()));
    }
}
