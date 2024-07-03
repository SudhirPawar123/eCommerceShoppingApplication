package com.jsp.onlineshoppingapplication.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jsp.onlineshoppingapplication.enums.UserRole;
import com.jsp.onlineshoppingapplication.requestdtos.AuthRequest;
import com.jsp.onlineshoppingapplication.requestdtos.OtpVerificationRequest;
import com.jsp.onlineshoppingapplication.requestdtos.UserRequest;
import com.jsp.onlineshoppingapplication.responsedtos.AuthResponse;
import com.jsp.onlineshoppingapplication.responsedtos.UserResponse;
import com.jsp.onlineshoppingapplication.service.UserService;
import com.jsp.onlineshoppingapplication.util.ResponseStructure;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@AllArgsConstructor
public class UserController {

	private final UserService userService;

	@PostMapping("/sellers/register")
	public ResponseEntity<ResponseStructure<UserResponse>> addSeller(@Valid @RequestBody UserRequest userRequest) {
		return userService.addUser(userRequest, UserRole.SELLER);
	}

	@PostMapping("/customers/register")
	public ResponseEntity<ResponseStructure<UserResponse>> addCustomer(@Valid @RequestBody UserRequest userRequest) {
		return userService.addUser(userRequest, UserRole.CUSTOMER);
	}

	@PostMapping("/otpverification")
	public ResponseEntity<ResponseStructure<UserResponse>> otpVerification(
			@RequestBody OtpVerificationRequest otpVerificationRequest) {
		return userService.otpVerification(otpVerificationRequest);
	}

	@PutMapping("/users/{userId}")
	@PreAuthorize("hasAuthority('CUSTOMER') OR hasAuthority('SELLER')")
	public ResponseEntity<ResponseStructure<UserResponse>> updateUser(@Valid @RequestBody UserRequest userRequest,
			@Valid @PathVariable Long userId) {
		return userService.updateUser(userRequest, userId);
	}

	@GetMapping("/users/{userId}")
	@PreAuthorize("hasAuthority('CUSTOMER') OR hasAuthority('SELLER')")
	public ResponseEntity<ResponseStructure<UserResponse>> findUser(@Valid @PathVariable Long userId) {
		return userService.findUser(userId);
	}

	@GetMapping("/users")
	@PreAuthorize("hasAuthority('CUSTOMER') OR hasAuthority('SELLER')")
	public ResponseEntity<ResponseStructure<List<UserResponse>>> findUsers() {
		return userService.findUsers();
	}

	@PostMapping("/login")
	public ResponseEntity<ResponseStructure<AuthResponse>> login(@RequestBody AuthRequest authRequest) {
		return userService.login(authRequest);
	}

	@PostMapping("/refreshLogin")
	@PreAuthorize("hasAuthority('CUSTOMER') OR hasAuthority('SELLER')")
	public ResponseEntity<ResponseStructure<AuthResponse>> refreshLogin(
			@CookieValue(value = "rt", required = false) String refreshToken) {
		return userService.refreshLogin(refreshToken);
	}

	@GetMapping("/test")
	public String test() {
		return "Success";
	}

}
