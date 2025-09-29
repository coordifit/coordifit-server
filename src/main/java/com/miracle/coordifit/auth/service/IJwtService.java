package com.miracle.coordifit.auth.service;

import java.time.LocalDateTime;

import com.miracle.coordifit.auth.model.User;

public interface IJwtService {
	String generateToken(User user, String tokenType);

	String getUserIdFromToken(String token);

	LocalDateTime getExpirationFromToken(String token);

	Boolean validateToken(String token);
}
