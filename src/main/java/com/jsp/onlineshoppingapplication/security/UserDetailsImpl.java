package com.jsp.onlineshoppingapplication.security;
//import com.ecommerce.shopping.user.entity.User;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.jsp.onlineshoppingapplication.entity.User;

public class UserDetailsImpl implements UserDetails {

    private final String username;
    private final String password;
    private final List<GrantedAuthority> grantedAuthorities;

    public UserDetailsImpl(User user) {
        username = user.getUsername();
        password = user.getPassword();
        grantedAuthorities = Arrays.stream(user.getUserRole().toString().split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return grantedAuthorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }
}