package com.jsp.onlineshoppingapplication.security;

import java.security.Key;
import java.util.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.jsp.onlineshoppingapplication.enums.UserRole;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

	@Value("${application.jwt.secret}")
	private String secret;

	private static final String ROLE="role";

	public String createJwtToken(String username, long expirationTimeInMillis,UserRole userRole) {
		return Jwts.builder()
				.setClaims(Map.of(ROLE,userRole))
				.setSubject(username)
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + expirationTimeInMillis))
				.signWith(getSignatureKey(), SignatureAlgorithm.HS512)
				.compact();
	}

	private Key getSignatureKey() {
		byte[] key = Decoders.BASE64.decode(secret);
		return Keys.hmacShaKeyFor(key);
	}

	private Claims parseJwtToken(String token) {
		return Jwts
				.parserBuilder()
				.setSigningKey(getSignatureKey())
				.build()
				.parseClaimsJws(token)
				.getBody();
	}

	public String extractUsername(String token) {
		return parseJwtToken(token)
				.getSubject();
	}

	public Date extractIssuDate(String token) {
		return parseJwtToken(token)
				.getIssuedAt();
	}

	public Date extractExpiryDate(String token) {
		return parseJwtToken(token)
				.getExpiration();
	}

	public UserRole extractUserRole(String token) {
		String role=parseJwtToken(token).get(ROLE,String.class);
		return UserRole.valueOf(role);
	}

}