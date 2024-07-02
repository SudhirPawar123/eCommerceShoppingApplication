package com.jsp.onlineshoppingapplication.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jsp.onlineshoppingapplication.entity.RefreshToken;


public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

}
