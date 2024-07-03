package com.jsp.onlineshoppingapplication.mapper;

import org.springframework.stereotype.Component;

import com.jsp.onlineshoppingapplication.entity.User;
import com.jsp.onlineshoppingapplication.requestdtos.UserRequest;
import com.jsp.onlineshoppingapplication.responsedtos.UserResponse;

import lombok.AllArgsConstructor;

@Component
public class UserMapper {

	public User mapUserRequestToUser(UserRequest userRequest, User user) {
		user.setEmail(userRequest.getEmail());
		user.setPassword(userRequest.getPassword());
		return user;
	}

	public UserResponse mapUserToUserResponse(User user) {
		return UserResponse.builder()
				.userId(user.getUserId())
				.username(user.getUsername())
				.email(user.getEmail())
				.userRole(user.getUserRole())
				.build();
	}

}
