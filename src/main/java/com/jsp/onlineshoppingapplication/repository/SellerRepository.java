package com.jsp.onlineshoppingapplication.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jsp.onlineshoppingapplication.entity.Seller;
import com.jsp.onlineshoppingapplication.enums.UserRole;

public interface SellerRepository extends JpaRepository<Seller, Long> {

	void save(UserRole userRole);

}
