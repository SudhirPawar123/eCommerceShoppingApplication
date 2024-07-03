package com.jsp.onlineshoppingapplication.exception;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class OtpExpiredException extends RuntimeException {
	private String message;
}