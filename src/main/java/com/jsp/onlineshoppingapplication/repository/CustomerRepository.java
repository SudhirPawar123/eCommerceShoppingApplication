package com.jsp.onlineshoppingapplication.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jsp.onlineshoppingapplication.entity.Customer;
import com.jsp.onlineshoppingapplication.enums.UserRole;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

//	void save(UserRole userRole);

}
