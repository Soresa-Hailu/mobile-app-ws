package com.appsdeveloperblog.ws.security;

import com.appsdeveloperblog.ws.SpringApplicationContext;

public class SecurityConstants {
	
	public  static final long EXPIRATION_TIME = 864000000; // valid 10 days
	public  static final long PASSWORD_REST_EXPIRATION_TIME = 3600000; // valid 1 hours
	public static final String TOKEN_PREFIX = "Bearer ";
	public static final String HEADER_STRING = "Authorization";
	public static final String SIGIN_UP_URL = "/users";
	public static final String VERIFICATION_EMAIL_URL ="/users/email-verification";
	public static final String PASSWORD_REST_REQUEST_URL ="/users/password-reset-request";
	public static final String PASSWORD_REST_URL ="/users/password-reset";
	public static final String H2_CONSOLE = "/h2-console/**";
	

	public static String getTokenSecret() {
		AppProperties appProperties  = (AppProperties) SpringApplicationContext.getBean("AppProperties");
		return appProperties.getTokenSecret();
	}

}
