package com.jsp.onlineshoppingapplication.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.jsp.onlineshoppingapplication.enums.UserRole;
import com.jsp.onlineshoppingapplication.requestdtos.OtpVerificationRequest;
import com.jsp.onlineshoppingapplication.requestdtos.UserRequest;
import com.jsp.onlineshoppingapplication.responsedtos.UserResponse;
import com.jsp.onlineshoppingapplication.util.ResponseStructure;

import lombok.AllArgsConstructor;

public interface UserService {



	
    ResponseEntity<ResponseStructure<UserResponse>> addUser(UserRequest userRequest, UserRole userRole);

    ResponseEntity<ResponseStructure<UserResponse>> updateUser(UserRequest userRequest, Long userId);

    ResponseEntity<ResponseStructure<UserResponse>> findUser(Long userId);

    ResponseEntity<ResponseStructure<List<UserResponse>>> findUsers();

	ResponseEntity<ResponseStructure<UserResponse>> otpVerification(OtpVerificationRequest otpVerificationRequest);

}
