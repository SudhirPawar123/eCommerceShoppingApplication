package com.jsp.onlineshoppingapplication.requestdtos;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OtpVerificationRequest {
	private String email;
	private String otp;
}
