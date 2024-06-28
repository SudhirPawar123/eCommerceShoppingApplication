package com.jsp.onlineshoppingapplication.serviceimpl;

import java.util.Date;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.google.common.cache.Cache;
import com.jsp.onlineshoppingapplication.cache.CacheConfig;
import com.jsp.onlineshoppingapplication.entity.Customer;
import com.jsp.onlineshoppingapplication.entity.Seller;
import com.jsp.onlineshoppingapplication.entity.User;
import com.jsp.onlineshoppingapplication.enums.UserRole;
import com.jsp.onlineshoppingapplication.exception.UserAlreadyExistException;
import com.jsp.onlineshoppingapplication.exception.UserNotExistException;
import com.jsp.onlineshoppingapplication.mapper.UserMapper;
import com.jsp.onlineshoppingapplication.repository.CustomerRepository;
import com.jsp.onlineshoppingapplication.repository.SellerRepository;
import com.jsp.onlineshoppingapplication.repository.UserRepository;
import com.jsp.onlineshoppingapplication.requestdtos.OtpVerificationRequest;
import com.jsp.onlineshoppingapplication.requestdtos.UserRequest;
import com.jsp.onlineshoppingapplication.responsedtos.UserResponse;
import com.jsp.onlineshoppingapplication.service.UserService;
import com.jsp.onlineshoppingapplication.util.MessageData;
import com.jsp.onlineshoppingapplication.util.ResponseStructure;

import jakarta.mail.MessagingException;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService{

	private final Cache<String,User> userCache;
	private final Cache<String,String> otpCache;
	private final Random random;

	private final UserRepository userRepository;

	private final UserMapper userMapper;

	private final SellerRepository sellerRepository;

	private final CustomerRepository customerRepository;

	private final MailService mailService; 

	private final MessageData messageData;

	@Override
	public ResponseEntity<ResponseStructure<UserResponse>> addUser(UserRequest userRequest, UserRole userRole) {
		boolean emailExist = userRepository.existsByEmail(userRequest.getEmail());
		if (emailExist) throw new UserAlreadyExistException("Email : " + userRequest.getEmail() + ", is already exist");
		else {
			User user=null;
			switch (userRole){
			case SELLER -> user=new Seller();
			case CUSTOMER -> user=new Customer();
			}
			if(user!=null) 
			{
				user= userMapper.mapUserRequestToUser(userRequest,user);
				user.setUserRole(userRole);
				userCache.put(user.getEmail(), user);
				otpCache.put(user.getEmail(), String.valueOf(random.nextInt(100000, 999999)));

				messageData.setTo(user.getEmail());
				messageData.setSubject("Otp Verification done by online shopping application");
				messageData.setSentDate(new Date());
				messageData.setText("otp"+" "+random.nextInt(100000, 999999));
				try {
					mailService.sendMail(messageData);
				} catch (MessagingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return ResponseEntity.status(HttpStatus.ACCEPTED).body(new ResponseStructure<UserResponse>()
						.setStatus(HttpStatus.ACCEPTED.value())
						.setMessage("Otp sended")
						.setData(userMapper.mapUserToUserResponse(user)));
			} else throw new UserAlreadyExistException("Bad Request");
		} 
	}

	@Override
	public ResponseEntity<ResponseStructure<UserResponse>> findUser(Long userId) {
		return userRepository.findById(userId).map(user -> {
			return ResponseEntity.status(HttpStatus.FOUND).body(new ResponseStructure<UserResponse>()
					.setStatus(HttpStatus.FOUND.value())
					.setMessage("User Founded")
					.setData(userMapper.mapUserToUserResponse(user)));
		}).orElseThrow(() -> new UserNotExistException("UserId : " + userId + ", is not exist"));
	}

	@Override
	public ResponseEntity<ResponseStructure<List<UserResponse>>> findUsers() {
		List<UserResponse> userResponseList = userRepository.findAll()
				.stream()
				.map(user -> userMapper.mapUserToUserResponse(user))
				.toList();
		return ResponseEntity.status(HttpStatus.FOUND).body(new ResponseStructure<List<UserResponse>>()
				.setMessage("Users are Founded")
				.setData(userResponseList));
	}

	@Override
	public ResponseEntity<ResponseStructure<UserResponse>> updateUser(UserRequest userRequest, Long userId) {
		return userRepository.findById(userId).map(user -> {
			user = userMapper.mapUserRequestToUser(userRequest, user);
			user = userRepository.save(user);
			return ResponseEntity.status(HttpStatus.FOUND).body(new ResponseStructure<UserResponse>()
					.setStatus(HttpStatus.FOUND.value())
					.setMessage("User Updated")
					.setData(userMapper.mapUserToUserResponse(user)));
		}).orElseThrow(() -> new UserNotExistException("UserId : " + userId + ", is not exist"));
	}

	@Override
	public ResponseEntity<ResponseStructure<UserResponse>> otpVerification(
			OtpVerificationRequest otpVerificationRequest) {
		User user=userCache.getIfPresent(otpVerificationRequest.getEmail());
		String otp=otpCache.getIfPresent(otpVerificationRequest.getEmail());
		System.out.println(user.getEmail());
		System.out.println(otp);
		return null;
	}

}
