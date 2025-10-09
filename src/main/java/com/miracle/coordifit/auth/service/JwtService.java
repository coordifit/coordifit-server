package com.miracle.coordifit.auth.service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.miracle.coordifit.user.model.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class JwtService implements IJwtService {

	@Value("${jwt.secret}")
	private String secret;

	@Value("${jwt.access-token-expiration:3600000}")
	private long accessTokenExpiration;

	@Value("${jwt.refresh-token-expiration:604800000}")
	private long refreshTokenExpiration;

	@Value("${jwt.issuer:coordifit}")
	private String issuer;

	private SecretKey secretKey;

	private SecretKey getSecretKey() {
		if (secretKey == null) {
			secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		}
		return secretKey;
	}

	@Override
	public String generateToken(User user, String tokenType) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("userId", user.getUserId());
		claims.put("email", user.getEmail());
		claims.put("nickname", user.getNickname());
		claims.put("fileId", user.getFileId());
		claims.put("loginTypeCode", user.getLoginTypeCode());
		claims.put("kakaoId", user.getKakaoId());
		claims.put("genderCode", user.getGenderCode());
		claims.put("birthDate", user.getBirthDate() != null ? user.getBirthDate().toString() : null);
		claims.put("type", tokenType);

		Date now = new Date();
		Date expiryDate = new Date(
			now.getTime() + (tokenType == "ACCESS" ? accessTokenExpiration : refreshTokenExpiration));

		JwtBuilder builder = Jwts.builder()
			.subject(user.getUserId())
			.issuer(issuer)
			.issuedAt(now)
			.expiration(expiryDate)
			.signWith(getSecretKey());

		for (Map.Entry<String, Object> entry : claims.entrySet()) {
			builder.claim(entry.getKey(), entry.getValue());
		}

		return builder.compact();
	}

	@Override
	public String getUserIdFromToken(String token) {
		Claims claims = getAllClaimsFromToken(token);
		return claims.getSubject();
	}

	@Override
	public LocalDateTime getExpirationFromToken(String token) {
		Claims claims = getAllClaimsFromToken(token);
		Date expiration = claims.getExpiration();
		return expiration.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
	}

	@Override
	public Boolean validateToken(String token) {
		try {
			getAllClaimsFromToken(token);
			return true;
		} catch (Exception e) {
			log.debug("JWT 토큰 검증 실패: {}", e.getMessage());
			return false;
		}
	}

	private Claims getAllClaimsFromToken(String token) {
		log.info("getAllClaimsFromToken : {}", token);
		try {
			return Jwts.parser()
				.verifyWith(secretKey)
				.build()
				.parseSignedClaims(token)
				.getPayload();
		} catch (ExpiredJwtException e) {
			log.warn("JWT 토큰이 만료되었습니다: {}", e.getMessage());
			throw e;
		} catch (UnsupportedJwtException e) {
			log.warn("지원하지 않는 JWT 토큰입니다: {}", e.getMessage());
			throw e;
		} catch (MalformedJwtException e) {
			log.warn("잘못된 JWT 토큰입니다: {}", e.getMessage());
			throw e;
		} catch (SecurityException e) {
			log.warn("JWT 토큰 서명이 유효하지 않습니다: {}", e.getMessage());
			throw e;
		} catch (IllegalArgumentException e) {
			log.warn("JWT 토큰이 비어있습니다: {}", e.getMessage());
			throw e;
		}
	}
}
