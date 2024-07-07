package com.jsp.onlineshoppingapplication.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jsp.onlineshoppingapplication.entity.AccessToken;
import com.jsp.onlineshoppingapplication.entity.User;


public interface AccessTokenRepository extends JpaRepository<AccessToken, Long> {

	Optional<AccessToken> findByToken(String at);

	List<AccessToken> findByUserAndIsBlocked(User user, boolean b);

	List<AccessToken> findByUserAndIsBlockedAndTokenNot(User user, boolean b, String accessToken);

}
