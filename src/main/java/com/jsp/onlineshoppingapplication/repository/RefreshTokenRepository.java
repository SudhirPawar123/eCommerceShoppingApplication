package com.jsp.onlineshoppingapplication.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jsp.onlineshoppingapplication.entity.RefreshToken;
import com.jsp.onlineshoppingapplication.entity.User;


public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

	Optional<RefreshToken> findByToken(String rt);

	List<RefreshToken> findByUserAndIsBlocked(User user, boolean b);

	List<RefreshToken> findByUserAndIsBlockedAndTokenNot(User user, boolean b, String refreshToken);


}
