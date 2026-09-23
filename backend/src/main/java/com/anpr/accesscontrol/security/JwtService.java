package com.anpr.accesscontrol.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Izdaje i validira JWT tokene za admin autentifikaciju.
 * Token nosi username i role kao claim-ove; nema potrebe da idemo u bazu
 * na svaki zahtev da proverimo ko je korisnik - to je poenta stateless
 * autentifikacije.
 */
@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationMillis;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms:3600000}") long expirationMillis
    ) {
        // Kljuc mora biti dovoljno dugacak za HS256 (minimum 256 bita / 32 bajta).
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMillis = expirationMillis;
    }

    public String generateToken(String username, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMillis);

        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();
    }

    /**
     * Vraca claim-ove ako je token validan (potpis i rok vazenja u redu),
     * ili baca izuzetak (JwtException/ExpiredJwtException) ako nije -
     * hvatanje tog izuzetka je posao filtera koji poziva ovaj servis.
     */
    public Claims parseAndValidate(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
