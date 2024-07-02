package com.jsp.onlineshoppingapplication.securityfilters;

import java.io.IOException;
import java.util.Arrays;

import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class LoginFilter extends OncePerRequestFilter {

	boolean loggedIn=false;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		Cookie[] cookies = request.getCookies();

		if(cookies!=null) {
		Arrays.asList(cookies).forEach(cookie -> {
			if(cookie.getName().equals("rt")||cookie.getName().equals("at"))
				loggedIn=true;
		});
		}
		if(loggedIn) {
			FilterException.handleJwtExpire(response,
					HttpStatus.UNAUTHORIZED.value(),
					"Failed to login",
					"logout First");
		}
		else
			filterChain.doFilter(request, response);
		
	}

}
