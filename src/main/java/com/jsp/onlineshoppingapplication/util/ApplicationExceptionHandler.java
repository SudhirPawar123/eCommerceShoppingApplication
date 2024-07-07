package com.jsp.onlineshoppingapplication.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.jsp.onlineshoppingapplication.exception.IllegalOperationException;
import com.jsp.onlineshoppingapplication.exception.InvalidOtpException;
import com.jsp.onlineshoppingapplication.exception.OtpExpiredException;
import com.jsp.onlineshoppingapplication.exception.TokenExpiredException;
import com.jsp.onlineshoppingapplication.exception.UserAlreadyExistException;
import com.jsp.onlineshoppingapplication.exception.UserNotExistException;
import com.jsp.onlineshoppingapplication.exception.UserNotLoggedInException;

@RestControllerAdvice
public class ApplicationExceptionHandler {

	private ResponseEntity<ErrorStructure<String>> errorResponse(HttpStatus status, String errorMessage,
			String rootCause) {
		return ResponseEntity.status(status).body(new ErrorStructure<String>().setStatus(status.value())
				.setMessage(errorMessage).setRootCause(rootCause));
	}

	@ExceptionHandler
	public ResponseEntity<ErrorStructure<String>> handleUserAlreadyExist(UserAlreadyExistException ex) {
		return errorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), "Already User is exist");
	}

	@ExceptionHandler
	public ResponseEntity<ErrorStructure<String>> handleUserNotExist(UserNotExistException ex) {
		return errorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), "User not exist");
	}

	@ExceptionHandler
	public ResponseEntity<ErrorStructure<String>> handleOtpExpired(OtpExpiredException ex) {
		return errorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), "Otp is expired");
	}

	@ExceptionHandler
	public ResponseEntity<ErrorStructure<String>> handleInvalidOpt(InvalidOtpException ex) {
		return errorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), "Otp Mismatch");
	}

	@ExceptionHandler
	public ResponseEntity<ErrorStructure<String>> handleIllegalOperation(IllegalOperationException ex) {
		return errorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(),
				"Illegal Operation ...Please fill correct information");
	}

	@ExceptionHandler
	public ResponseEntity<ErrorStructure<String>> handleTokenExpired(TokenExpiredException ex) {
		return errorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), "Token Expired");
	}
	
	@ExceptionHandler
	public ResponseEntity<ErrorStructure<Map<String, String>>> handleMethodArgumentNotValid(
			MethodArgumentNotValidException ex) {
		List<ObjectError> objErrors = ex.getAllErrors();
		Map<String, String> allErrors = new HashMap<>();
		objErrors.forEach(error -> {
			FieldError fieldError = (FieldError) error;
			String field = fieldError.getField();
			String message = fieldError.getDefaultMessage();
			allErrors.put(field, message);
		});

		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorStructure<Map<String, String>>()
				.setStatus(HttpStatus.NOT_FOUND.value()).setMessage("Invalid Input").setRootCause(allErrors));
	}
}