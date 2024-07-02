package com.jsp.onlineshoppingapplication.responsedtos;

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
public class AuthResponse {
	private long userId;
	private String username;
	private long accessExpiration;
	private long refreshExpiration;
}
