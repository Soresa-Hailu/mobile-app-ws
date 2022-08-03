package com.appsdeveloperblog.ws.exceptions;

public class UserServiceException extends RuntimeException{

	private static final long serialVersionUID = -6845007273139721665L;

	public UserServiceException(String message) {
		super(message);
	}
}
