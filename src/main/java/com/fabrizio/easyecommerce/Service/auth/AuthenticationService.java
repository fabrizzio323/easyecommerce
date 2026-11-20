package com.fabrizio.easyecommerce.Service.auth;
import lombok.RequiredArgsConstructor;

import com.fabrizio.easyecommerce.Service.jwt.JwtService;
import com.fabrizio.easyecommerce.dto.authentication.AuthenticationRequest;
import com.fabrizio.easyecommerce.dto.authentication.AuthenticationResponse;
import com.fabrizio.easyecommerce.dto.authentication.RegisterRequest;
import com.fabrizio.easyecommerce.dto.authentication.UserDTO;
import com.fabrizio.easyecommerce.entity.User;
import com.fabrizio.easyecommerce.enums.Role;
import com.fabrizio.easyecommerce.mapper.user.UserMapper;
import com.fabrizio.easyecommerce.repository.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);


    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncode;


    public void register(RegisterRequest registerRequest){
        logger.info("Starting user registration for email: {}", registerRequest.getEmail());

        if(emailExistent(registerRequest.getEmail()).isEmpty())  {
            logger.debug("Email {} is available, proceeding with registration", registerRequest.getEmail());
            User user = new User();
            user.setName(registerRequest.getName());
            user.setEmail(registerRequest.getEmail());
            user.setRole(Role.USER);
            user.setPassword(passwordEncode.encode(registerRequest.getPassword()));
            user.setStreet(registerRequest.getStreet());
            user.setCity(registerRequest.getCity());
            user.setZipCode(registerRequest.getZipCode());
            userRepository.save(user);
            logger.info("User registered successfully: {}", registerRequest.getEmail());
        }else{
            logger.warn("Registration failed: Email already exists: {}", registerRequest.getEmail());
            throw new RuntimeException("Email already exists");
        }

    }


    public AuthenticationResponse login(AuthenticationRequest request){
        logger.info("Login attempt for user: {}", request.getEmail());
        try {
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    request.getEmail(), request.getPassword()
            );
            authenticationManager.authenticate(authToken);
            logger.debug("Authentication successful for user: {}", request.getEmail());

            User user = userRepository.findByEmail(request.getEmail()).get();
            logger.debug("User found in database: {}", user.getEmail());

            String jwt = jwtService.generateToken(user.getEmail(), generateExtraClaims(user));
            logger.info("Login successful, JWT generated for user: {}", request.getEmail());
            return new AuthenticationResponse(jwt);
        } catch (Exception e) {
            logger.warn("Login failed for user: {}", request.getEmail(), e);
            throw e;
        }
    }

    public UserDTO authMe(String jwt){
        logger.debug("Auth-me service called");
        if(jwt==null || !jwt.startsWith("Bearer ")){
            logger.warn("Invalid token format provided");
            throw new RuntimeException("Invalid token");
        }
        String email = jwtService.extractEmail(jwt);
        logger.debug("Extracted email from token: {}", email);

        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        logger.info("Auth-me successful for user: {}", email);

        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setName(user.getName());
        userDTO.setEmail(user.getEmail());
        userDTO.setRole(user.getRole());
        return userDTO;
    }

    private Map<String, Object> generateExtraClaims(User user){
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("name", user.getName());
        extraClaims.put("role", user.getRole());
        extraClaims.put("permissions", user.getAuthorities());
        return extraClaims;
    }

    private Optional<User> emailExistent(String email){
        return userRepository.findByEmail(email);
    }


}