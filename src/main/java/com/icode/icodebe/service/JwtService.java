package com.icode.icodebe.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.issuer}")
    private String issuer;

    @Value("${jwt.signing.key}")
    private String signingKey;

    private String generateJwt(String subject) {
        final var expirationDate = calculateExpirationDate();
        final var signingKey = createSigningKey();

        return Jwts.builder()
                .setSubject(subject)
                .setExpiration(expirationDate)
                .setIssuedAt(new Date())
                .setIssuer(issuer)
                .signWith(signingKey)
                .compact();
    }

    public String getSubject(String jwt) {
        final var signingKey = createSigningKey();
        final var jwtParser = Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .requireIssuer(issuer)
                .build();

        return jwtParser.parseClaimsJws(jwt)
                .getBody()
                .getSubject();
    }

    private Key createSigningKey() {
        return Keys.hmacShaKeyFor(signingKey.getBytes());
    }

    private Date calculateExpirationDate() {
        final var expirationInstant = LocalDateTime.now()
                .plusDays(3)
                .atZone(ZoneId.systemDefault())
                .toInstant();

        return Date.from(expirationInstant);
    }
}
