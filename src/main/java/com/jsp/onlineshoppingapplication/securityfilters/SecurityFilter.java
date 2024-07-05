package com.jsp.onlineshoppingapplication.securityfilters;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import com.jsp.onlineshoppingapplication.entity.AccessToken;
import com.jsp.onlineshoppingapplication.entity.RefreshToken;
import com.jsp.onlineshoppingapplication.enums.UserRole;
import com.jsp.onlineshoppingapplication.repository.AccessTokenRepository;
import com.jsp.onlineshoppingapplication.repository.RefreshTokenRepository;
import com.jsp.onlineshoppingapplication.security.JwtService;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SecurityFilter extends OncePerRequestFilter {

	private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AccessTokenRepository accessTokenRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String rt = null;
        String at = null;

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("rt"))
                    rt = cookie.getValue();
                else if (cookie.getName().equals("at"))
                    at = cookie.getValue();
            }
        }
        
        if (at != null && rt != null) {
            Optional<RefreshToken> refreshToken = refreshTokenRepository.findByToken(rt);
            Optional<AccessToken> accessToken = accessTokenRepository.findByToken(at);

            if (!refreshToken.get().isBlocked() && !accessToken.get().isBlocked()) {
                try {
                    Date expireDate = jwtService.extractExpiryDate(at);
                    String username = jwtService.extractUsername(at);
                    UserRole userRole = jwtService.extractUserRole(at);

                    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        UsernamePasswordAuthenticationToken upat = new UsernamePasswordAuthenticationToken(username, null, List.of(new SimpleGrantedAuthority(userRole.name())));
                        upat.setDetails(new WebAuthenticationDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(upat);
                    }
                } catch (ExpiredJwtException e) {
                    FilterException.handleJwtExpire(response,
                            HttpStatus.UNAUTHORIZED.value(),
                            "Failed to authenticate",
                            "Token has already expired");
                    return;
                } catch (JwtException e) {
                    FilterException.handleJwtExpire(response,
                            HttpStatus.UNAUTHORIZED.value(),
                            "Failed to authenticate",
                            "you are not allowed to access this resource");
                    return;
                }
            } else {
                FilterException.handleJwtExpire(response,
                        HttpStatus.UNAUTHORIZED.value(),
                        "Failed to authenticate",
                        "Please login first your token is expired");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
