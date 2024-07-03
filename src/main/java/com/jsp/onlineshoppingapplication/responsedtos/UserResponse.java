package com.jsp.onlineshoppingapplication.responsedtos;

import com.jsp.onlineshoppingapplication.enums.UserRole;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {
	private long userId;
	private String username;
	private String email;
	private UserRole userRole;
}
