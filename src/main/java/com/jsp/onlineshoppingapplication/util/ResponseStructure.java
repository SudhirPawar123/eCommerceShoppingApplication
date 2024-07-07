package com.jsp.onlineshoppingapplication.util;

public class ResponseStructure<T> {
	private long status;
	private String message;
	private T data;
	public long getStatus() {
		return status;
	}
	public ResponseStructure<T> setStatus(long status) {
		this.status = status;
		return this;
	}
	public String getMessage() {
		return message;
	}
	public ResponseStructure<T> setMessage(String message) {
		this.message = message;
		return this;

	}
	public T getData() {
		return data;
	}
	public ResponseStructure<T> setData(T data) {
		this.data = data;
		return this;
	}
}
