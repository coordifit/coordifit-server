package com.miracle.coordifit.auth.filter;

import java.io.IOException;
import java.util.ArrayList;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.miracle.coordifit.auth.service.IJwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final IJwtService jwtService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
		FilterChain filterChain) throws ServletException, IOException {

		String path = request.getRequestURI();

		// 🔹 Swagger 및 API 문서 요청은 필터에서 제외
		if (path.startsWith("/swagger-ui")
			|| path.startsWith("/v3/api-docs")
			|| path.startsWith("/swagger-resources")) {
			filterChain.doFilter(request, response);
			return;
		}

		final String authHeader = request.getHeader("Authorization");
		final String jwt;
		final String userEmail;

		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}

		jwt = authHeader.substring(7);

		try {
			if (!jwtService.validateToken(jwt)) {
				log.debug("JWT 토큰이 유효하지 않음 (만료 또는 형식 오류)");
				// 토큰이 있지만 유효하지 않음 → 401 (갱신 가능)
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"토큰이 만료되었거나 유효하지 않습니다.\"}");
				response.setContentType("application/json");
				return;
			}

			// JWT 토큰에서 사용자 이메일 추출
			userEmail = jwtService.getUserIdFromToken(jwt);

			// 사용자 이메일이 있고 아직 인증되지 않은 경우
			if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

				// 간단한 인증 토큰 생성 (권한 없음)
				UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
					userEmail, null, new ArrayList<>());
				authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

				// SecurityContext에 인증 정보 설정
				SecurityContextHolder.getContext().setAuthentication(authToken);

				log.debug("JWT 토큰 인증 성공: {}", userEmail);
			}
		} catch (Exception e) {
			log.debug("JWT 토큰 처리 중 오류 발생 (만료된 토큰일 수 있음): {}", e.getMessage());
			// 토큰 파싱 에러 → 401 (갱신 가능)
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().write("{\"error\":\"Unauthorized\",\"message\":\"토큰 처리 중 오류가 발생했습니다.\"}");
			response.setContentType("application/json");
			return;
		}

		filterChain.doFilter(request, response);
	}
}
