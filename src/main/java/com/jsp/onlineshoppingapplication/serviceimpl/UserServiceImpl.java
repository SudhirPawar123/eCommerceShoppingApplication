package com.jsp.onlineshoppingapplication.serviceimpl;

import java.util.Date;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.google.common.cache.Cache;
import com.jsp.onlineshoppingapplication.cache.CacheConfig;
import com.jsp.onlineshoppingapplication.entity.Customer;
import com.jsp.onlineshoppingapplication.entity.Seller;
import com.jsp.onlineshoppingapplication.entity.User;
import com.jsp.onlineshoppingapplication.enums.UserRole;
import com.jsp.onlineshoppingapplication.exception.IllegalOperationException;
import com.jsp.onlineshoppingapplication.exception.InvalidOtpException;
import com.jsp.onlineshoppingapplication.exception.OtpExpiredException;
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

    private final PasswordEncoder passwordEncoder;

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
				int otp=random.nextInt(100000, 999999);
				otpCache.put(user.getEmail(), String.valueOf(otp));
				
                mailSend(user.getEmail(), "OTP verification for EcommerceShoppingApp", "<h3>Welcome to Ecommerce Shopping Applicationa</h3></br><h4>Otp : " + otp + "</h4>");
				return ResponseEntity.status(HttpStatus.ACCEPTED).body(new ResponseStructure<UserResponse>()
						.setStatus(HttpStatus.ACCEPTED.value())
						.setMessage("Otp sended")
						.setData(userMapper.mapUserToUserResponse(user)));
			} else throw new UserAlreadyExistException("Bad Request");
		} 
	}
	
    private void mailSend(String email, String subject, String text) {
        MessageData messageData = new MessageData();
        messageData.setTo(email);
        messageData.setSubject(subject);
        messageData.setText(text);
        messageData.setSentDate(new Date());
        try {
            mailService.sendMail(messageData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	@Override
	public ResponseEntity<ResponseStructure<UserResponse>> otpVerification(
			OtpVerificationRequest otpVerificationRequest) {
		 User user = userCache.getIfPresent(otpVerificationRequest.getEmail());
	        String otp = otpCache.getIfPresent(otpVerificationRequest.getEmail());
	        if (user == null && otp == null) {
	            throw new IllegalOperationException("Please Enter correct information");
	        } else if (otp == null && user.getEmail().equals(otpVerificationRequest.getEmail())) {
//	            if user otp will be expired
	            throw new OtpExpiredException("Otp is expired");
	        } else if (!otp.equals(otpVerificationRequest.getOtp())) {
//	            oto mismatch with existing otp   or   invalid otp
	            throw new InvalidOtpException("Invalid otp");
	        } else if (otp.equals(otpVerificationRequest.getOtp())) {
//	            If user otp and cache otp
//	           Create Dynamic username
	            String userGen = usernameGenerate(user.getEmail());
	            user.setUsername(userGen);
	            user.setPassword(passwordEncoder.encode(user.getPassword()));
	            user = userRepository.save(user);

//	            Send mail to user for confirmation
	            mailSend(user.getEmail(), "Email Verification done", "<h4>Your account is create in EcommerceShoppingApp</h4></br></br> Your username is : " + "<h5>" + userGen + "</h5>");

	            return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseStructure<UserResponse>()
	                    .setStatus(HttpStatus.CREATED.value())
	                    .setMessage(user.getUserRole() + " Created")
	                    .setData(userMapper.mapUserToUserResponse(user)));
	        } else {
	            throw new OtpExpiredException("Otp is expired");
	        }
	    }
	
    private String usernameGenerate(String email) {
        String[] str = email.split("@");
        String username = str[0];
        int temp = 0;
        while (true) {
            if (userRepository.existsByUsername(username)) {
                username += temp;
                temp++;
                continue;
            } else
                break;
        }
        if (temp != 0) {
            return username;
        } else {
            return str[0];
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



}
