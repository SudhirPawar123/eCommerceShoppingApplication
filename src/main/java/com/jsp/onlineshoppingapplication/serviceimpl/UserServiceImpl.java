package com.jsp.onlineshoppingapplication.serviceimpl;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.google.common.cache.Cache;
import com.jsp.onlineshoppingapplication.entity.AccessToken;
import com.jsp.onlineshoppingapplication.entity.Customer;
import com.jsp.onlineshoppingapplication.entity.RefreshToken;
import com.jsp.onlineshoppingapplication.entity.Seller;
import com.jsp.onlineshoppingapplication.entity.User;
import com.jsp.onlineshoppingapplication.enums.UserRole;
import com.jsp.onlineshoppingapplication.exception.IllegalOperationException;
import com.jsp.onlineshoppingapplication.exception.InvalidOtpException;
import com.jsp.onlineshoppingapplication.exception.OtpExpiredException;
import com.jsp.onlineshoppingapplication.exception.TokenExpiredException;
import com.jsp.onlineshoppingapplication.exception.UserAlreadyExistException;
import com.jsp.onlineshoppingapplication.exception.UserNotExistException;
import com.jsp.onlineshoppingapplication.exception.UserNotLoggedInException;
import com.jsp.onlineshoppingapplication.mapper.UserMapper;
import com.jsp.onlineshoppingapplication.repository.AccessTokenRepository;
import com.jsp.onlineshoppingapplication.repository.RefreshTokenRepository;
import com.jsp.onlineshoppingapplication.repository.UserRepository;
import com.jsp.onlineshoppingapplication.requestdtos.AuthRequest;
import com.jsp.onlineshoppingapplication.requestdtos.OtpVerificationRequest;
import com.jsp.onlineshoppingapplication.requestdtos.UserRequest;
import com.jsp.onlineshoppingapplication.responsedtos.AuthResponse;
import com.jsp.onlineshoppingapplication.responsedtos.UserResponse;
import com.jsp.onlineshoppingapplication.security.JwtService;
import com.jsp.onlineshoppingapplication.service.UserService;
import com.jsp.onlineshoppingapplication.util.MessageData;
import com.jsp.onlineshoppingapplication.util.ResponseStructure;

@Service
public class UserServiceImpl implements UserService {

	@Value("${application.jwt.access_expiry_seconds}")
	private long accessExpirySeconds;

	@Value("${application.jwt.refresh_expiry_seconds}")
	private long refreshExpirySeconds;

	@Value("${application.cookie.domain}")
	private String domain;

	@Value("${application.cookie.same_site}")
	private String sameSite;

	@Value("${application.cookie.secure}")
	private Boolean secure;

	private final Cache<String, User> userCache;
	private final Cache<String, String> otpCache;
	private final Random random;
	private final PasswordEncoder passwordEncoder;
	private final UserRepository userRepository;
	private final UserMapper userMapper;
	private final MailService mailService;
	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;
	private final RefreshTokenRepository refreshTokenRepository;
	private AccessTokenRepository accessTokenRepository;

	public UserServiceImpl(Cache<String, User> userCache,
			Cache<String, String> otpCache,
			Random random,
			PasswordEncoder passwordEncoder, 
			UserRepository userRepository,
			UserMapper userMapper,
			MailService mailService,
			AuthenticationManager authenticationManager, 
			JwtService jwtService,
			RefreshTokenRepository refreshTokenRepository, 
			AccessTokenRepository accessTokenRepository) 
	{
		this.userCache = userCache;
		this.otpCache = otpCache;
		this.random = random;
		this.passwordEncoder = passwordEncoder;
		this.userRepository = userRepository;
		this.userMapper = userMapper;
		this.mailService = mailService;
		this.authenticationManager = authenticationManager;
		this.jwtService = jwtService;
		this.refreshTokenRepository = refreshTokenRepository;
		this.accessTokenRepository = accessTokenRepository;
	}

	@Override
	public ResponseEntity<ResponseStructure<UserResponse>> addUser(UserRequest userRequest, UserRole userRole) {
		boolean emailExist = userRepository.existsByEmail(userRequest.getEmail());
		if (emailExist)
			throw new UserAlreadyExistException("Email : " + userRequest.getEmail() + ", is already exist");
		else {
			User user = null;
			switch (userRole) {
			case SELLER -> user = new Seller();
			case CUSTOMER -> user = new Customer();
			}
			if (user != null) {
				user = userMapper.mapUserRequestToUser(userRequest, user);
				user.setUserRole(userRole);
				userCache.put(user.getEmail(), user);
				int otp = random.nextInt(100000, 999999);
				otpCache.put(user.getEmail(), String.valueOf(otp));

				mailSend(user.getEmail(), "OTP verification for EcommerceShoppingApp",
						"<h3>Welcome to Ecommerce Shopping Applicationa</h3></br><h4>Otp : " + otp + "</h4>");
				return ResponseEntity.status(HttpStatus.ACCEPTED)
						.body(new ResponseStructure<UserResponse>().setStatus(HttpStatus.ACCEPTED.value())
								.setMessage("Otp sended").setData(userMapper.mapUserToUserResponse(user)));
			} else
				throw new UserAlreadyExistException("Bad Request");
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
			// if user otp will be expired
			throw new OtpExpiredException("Otp is expired");
		} else if (!otp.equals(otpVerificationRequest.getOtp())) {
			// oto mismatch with existing otp or invalid otp
			throw new InvalidOtpException("Invalid otp");
		} else if (otp.equals(otpVerificationRequest.getOtp())) {
			// If user otp and cache otp
			// Create Dynamic username
			String userGen = usernameGenerate(user.getEmail());
			user.setUsername(userGen);
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			user = userRepository.save(user);

			// Send mail to user for confirmation
			mailSend(user.getEmail(), "Email Verification done",
					"<h4>Your account is create in EcommerceShoppingApp</h4></br></br> Your username is : " + "<h5>"
							+ userGen + "</h5>");

			return ResponseEntity.status(HttpStatus.CREATED)
					.body(new ResponseStructure<UserResponse>().setStatus(HttpStatus.CREATED.value())
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
			return ResponseEntity.status(HttpStatus.FOUND)
					.body(new ResponseStructure<UserResponse>().setStatus(HttpStatus.FOUND.value())
							.setMessage("User Founded").setData(userMapper.mapUserToUserResponse(user)));
		}).orElseThrow(() -> new UserNotExistException("UserId : " + userId + ", is not exist"));
	}

	@Override
	public ResponseEntity<ResponseStructure<List<UserResponse>>> findUsers() {
		List<UserResponse> userResponseList = userRepository.findAll().stream()
				.map(user -> userMapper.mapUserToUserResponse(user)).toList();
		return ResponseEntity.status(HttpStatus.FOUND).body(
				new ResponseStructure<List<UserResponse>>().setMessage("Users are Founded").setData(userResponseList));
	}

	@Override
	public ResponseEntity<ResponseStructure<UserResponse>> updateUser(UserRequest userRequest, Long userId) {
		return userRepository.findById(userId).map(user -> {
			user = userMapper.mapUserRequestToUser(userRequest, user);
			user = userRepository.save(user);
			return ResponseEntity.status(HttpStatus.FOUND)
					.body(new ResponseStructure<UserResponse>().setStatus(HttpStatus.FOUND.value())
							.setMessage("User Updated").setData(userMapper.mapUserToUserResponse(user)));
		}).orElseThrow(() -> new UserNotExistException("UserId : " + userId + ", is not exist"));
	}

	@Override
	public ResponseEntity<ResponseStructure<AuthResponse>> login(AuthRequest authRequest) {
		try {
			Authentication authenticate = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));
			if (authenticate.isAuthenticated()) {

				return userRepository.findByUsername(authRequest.getUsername()).map((existUser)->{
					HttpHeaders httpHeaders = new HttpHeaders(); 
					grantAccessToken(httpHeaders ,existUser);
					grantRefreshToken(httpHeaders ,existUser);

					return ResponseEntity.status(HttpStatus.OK)
							.headers(httpHeaders)
							.body(new ResponseStructure<AuthResponse>()
									.setStatus(HttpStatus.OK.value())
									.setMessage("Token created")
									.setData(AuthResponse.builder()
											.userId(existUser.getUserId())
											.username(existUser.getUsername())
											.accessExpiration(accessExpirySeconds)
											.refreshExpiration(refreshExpirySeconds)
											.build()));
				}).orElseThrow(()->new UserNotExistException("Username : "+authRequest.getUsername()+", not exist"));
			}

			else
				throw new BadCredentialsException("Invalid Credentials");
		} catch (AuthenticationException e) {
			throw new BadCredentialsException("Invalid Credentials", e);
		}
	}

	private void grantAccessToken(HttpHeaders httpHeaders, User user) {
		String token = jwtService.createJwtToken(user.getUsername(), accessExpirySeconds,user.getUserRole());

		AccessToken accessToken = AccessToken.builder()
				.token(token)
				.expiration(LocalDateTime.now().plusSeconds(accessExpirySeconds))
				.user(user)
				.build();

		accessTokenRepository.save(accessToken);

		httpHeaders.add(HttpHeaders.SET_COOKIE, generateCookie("at", token, accessExpirySeconds));

	}

	private void grantRefreshToken(HttpHeaders httpHeaders, User user) {
		String token = jwtService.createJwtToken(user.getUsername(), refreshExpirySeconds,user.getUserRole()); // refreshExpirySeconds = (60 * 60 * 24 * 15L) = 1296000

		RefreshToken refreshToken = RefreshToken.builder()
				.token(token)
				.expiration(LocalDateTime.now().plusSeconds(refreshExpirySeconds))
				.user(user)
				.build();

		refreshTokenRepository.save(refreshToken);

		httpHeaders.add(HttpHeaders.SET_COOKIE, generateCookie("rt", token, refreshExpirySeconds));
	}

	private String generateCookie(String name, String value, long maxAge) {
		return ResponseCookie.from(name, value)
				.domain(domain)
				.path("/")
				.maxAge(maxAge)
				.sameSite(sameSite)
				.httpOnly(true)
				.secure(secure)
				.build()
				.toString();
	}

	@Override
	public ResponseEntity<ResponseStructure<AuthResponse>> refreshLogin(String refreshToken) {
		if(refreshToken == null)
			throw new UserNotLoggedInException("Please login first");

		Date expiryDate = jwtService.extractExpiryDate(refreshToken);
		if (expiryDate.getTime() < new Date().getTime()) {
			throw new TokenExpiredException("Refresh token was expired, Please make a new SignIn request");
		} else {
			String username = jwtService.extractUsername(refreshToken);
			User user = userRepository.findByUsername(username).get();

			HttpHeaders httpHeaders = new HttpHeaders();
			grantAccessToken(httpHeaders, user);

			return ResponseEntity.status(HttpStatus.OK)
					.headers(httpHeaders)
					.body(new ResponseStructure<AuthResponse>()
							.setStatus(HttpStatus.OK.value())
							.setMessage("Access Toke renewed")
							.setData(AuthResponse.builder()
									.userId(user.getUserId())
									.username(user.getUsername())
									.accessExpiration(accessExpirySeconds)
									.refreshExpiration((expiryDate.getTime() - new Date().getTime())/1000)
									.build()));
		}
	}
}
