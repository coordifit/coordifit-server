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

import com.miracle.coordifit.auth.repository.JwtTokenRepository;
import com.miracle.coordifit.user.model.User;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService implements IJwtService {

	@Value("${jwt.secret}")
	private String secret;

	@Value("${jwt.access-token-expiration:3600000}")
	private long accessTokenExpiration;

	@Value("${jwt.refresh-token-expiration:604800000}")
	private long refreshTokenExpiration;

	@Value("${jwt.issuer:coordifit}")
	private String issuer;

	private final JwtTokenRepository jwtTokenRepository;
	private SecretKey secretKey;

	@PostConstruct
	public void init() {
		log.info("=== JWT 설정 확인 ===");
		log.info("Secret Key: {}", secret != null ? "설정됨 (길이: " + secret.length() + ")" : "❌ NULL!!");
		log.info("Access Token Expiration: {} ms ({} 시간)", accessTokenExpiration, accessTokenExpiration / 3600000.0);
		log.info("Refresh Token Expiration: {} ms ({} 일)", refreshTokenExpiration, refreshTokenExpiration / 86400000.0);
		log.info("Issuer: {}", issuer);

		if (secret == null || secret.isEmpty()) {
			log.error("❌❌❌ JWT Secret Key가 설정되지 않았습니다! application.properties 또는 application-local.properties를 확인하세요!");
		}
	}

	private SecretKey getSecretKey() {
		if (secretKey == null) {
			secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		}
		return secretKey;
	}

	private String generateToken(User user, String tokenType) {
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

	@Override
	public Map<String, Object> createTokens(User user) {
		log.info("사용자 로그인 토큰 생성: {}", user.getUserId());

		// JWT 토큰 생성
		String accessToken = generateToken(user, "ACCESS");
		String refreshToken = generateToken(user, "REFRESH");

		// 기존 토큰 삭제 (보안을 위해)
		jwtTokenRepository.deleteToken(user.getUserId(), "access");
		jwtTokenRepository.deleteToken(user.getUserId(), "refresh");

		// 새 토큰들을 Redis에 저장
		jwtTokenRepository.saveToken(user.getUserId(), accessToken, getExpirationFromToken(accessToken), "access");
		jwtTokenRepository.saveToken(user.getUserId(), refreshToken, getExpirationFromToken(refreshToken), "refresh");

		// 응답 데이터 준비
		Map<String, Object> responseData = new HashMap<>();
		responseData.put("accessToken", accessToken);
		responseData.put("refreshToken", refreshToken);
		responseData.put("tokenType", "Bearer");

		return responseData;
	}

	@Override
	public Map<String, Object> refreshAccessToken(User user) {
		log.info("액세스 토큰 갱신: {}", user.getUserId());

		// 새 액세스 토큰 생성
		String newAccessToken = generateToken(user, "ACCESS");

		// 기존 액세스 토큰 삭제
		jwtTokenRepository.deleteToken(user.getUserId(), "access");

		// 새 액세스 토큰 저장
		jwtTokenRepository.saveToken(user.getUserId(), newAccessToken, getExpirationFromToken(newAccessToken),
			"access");

		// 응답 데이터 준비
		Map<String, Object> responseData = new HashMap<>();
		responseData.put("accessToken", newAccessToken);
		responseData.put("tokenType", "Bearer");

		return responseData;
	}

	@Override
	public void deleteAllUserTokens(String userId) {
		log.info("사용자 모든 토큰 삭제: {}", userId);

		// 해당 사용자의 모든 토큰 삭제
		jwtTokenRepository.deleteToken(userId, "access");
		jwtTokenRepository.deleteToken(userId, "refresh");
	}

	private Claims getAllClaimsFromToken(String token) {
		if (log.isDebugEnabled()) {
			String maskedToken = token == null ? null
				: token.substring(0, Math.min(10, token.length())) + "...";
			log.debug("getAllClaimsFromToken : {}", maskedToken);
		}
		try {
			return Jwts.parser()
				.verifyWith(getSecretKey())
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
