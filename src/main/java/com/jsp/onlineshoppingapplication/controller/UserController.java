package com.jsp.onlineshoppingapplication.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jsp.onlineshoppingapplication.enums.UserRole;
import com.jsp.onlineshoppingapplication.requestdtos.OtpVerificationRequest;
import com.jsp.onlineshoppingapplication.requestdtos.UserRequest;
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
	  public ResponseEntity<ResponseStructure<UserResponse>>  otpVerification(@RequestBody OtpVerificationRequest otpVerificationRequest){
	    	return userService.otpVerification(otpVerificationRequest);
	    }
	    
	     @PutMapping("/users/{userId}")
	     public ResponseEntity<ResponseStructure<UserResponse>> updateUser(
	            @Valid @RequestBody UserRequest userRequest,
	            @Valid @PathVariable Long userId){
	        return userService.updateUser(userRequest, userId);
	     }
	    @GetMapping("/users/{userId}")
	    public ResponseEntity<ResponseStructure<UserResponse>> findUser(@Valid @PathVariable Long userId) {
	        return userService.findUser(userId);
	    }

	    @GetMapping("/users")
	    public ResponseEntity<ResponseStructure<List<UserResponse>>> findUsers() {
	        return userService.findUsers();
	    }

}
