package com.jsp.onlineshoppingapplication.security;
//import com.ecommerce.shopping.user.repositoty.UserRepository;

//import lombok.AllArgsConstructor;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.jsp.onlineshoppingapplication.repository.UserRepository;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class UserDetailServiceImpl implements UserDetailsService {

	private final UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return userRepository.findByUsername(username).map(UserDetailsImpl::new)
				.orElseThrow(() -> new UsernameNotFoundException("Username :" + username + ", not found"));
	}
}