package com.jsp.onlineshoppingapplication.securityfilters;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import com.jsp.onlineshoppingapplication.entity.RefreshToken;
import com.jsp.onlineshoppingapplication.enums.UserRole;
import com.jsp.onlineshoppingapplication.repository.RefreshTokenRepository;
import com.jsp.onlineshoppingapplication.security.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class RefreshFilter extends OncePerRequestFilter {
	private final JwtService jwtService;
	private final RefreshTokenRepository refreshTokenRepository;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		Cookie[] cookies = request.getCookies();
		String rt = null;
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals("rt"))
					rt = cookie.getValue();
			}
		} else {
			FilterException.handleJwtExpire(response, HttpStatus.UNAUTHORIZED.value(), "Failed to check refresh token",
					"Refresh Token is not present");
		}

		Optional<RefreshToken> refreshToken = refreshTokenRepository.findByToken(rt);
		if (refreshToken.isPresent() && !refreshToken.get().isBlocked()) {
			String username = jwtService.extractUsername(rt);
			UserRole userRole = jwtService.extractUserRole(rt);

			if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
				UsernamePasswordAuthenticationToken upat = new UsernamePasswordAuthenticationToken(username, null,
						List.of(new SimpleGrantedAuthority(userRole.name())));
				upat.setDetails(new WebAuthenticationDetails(request));
				SecurityContextHolder.getContext().setAuthentication(upat);
			}
		} else {
			FilterException.handleJwtExpire(response, HttpStatus.UNAUTHORIZED.value(), "Failed to check refresh token",
					"Refresh Token is already expired");
		}

		filterChain.doFilter(request, response);
	}
}
