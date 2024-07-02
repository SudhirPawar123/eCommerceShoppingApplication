package com.jsp.onlineshoppingapplication.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jsp.onlineshoppingapplication.entity.AccessToken;


public interface AccessTokenRepository extends JpaRepository<AccessToken, Long> {

}
