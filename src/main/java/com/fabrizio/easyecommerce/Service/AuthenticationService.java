package com.fabrizio.easyecommerce.Service;

import com.fabrizio.easyecommerce.dto.AuthenticationRequest;
import com.fabrizio.easyecommerce.dto.AuthenticationResponse;
import com.fabrizio.easyecommerce.dto.RegisterRequest;
import com.fabrizio.easyecommerce.dto.UserDTO;
import com.fabrizio.easyecommerce.entity.User;
import com.fabrizio.easyecommerce.enums.Role;
import com.fabrizio.easyecommerce.mapper.UserMapper;
import com.fabrizio.easyecommerce.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
public class AuthenticationService {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PasswordEncoder passwordEncode;


    public void register(RegisterRequest registerRequest){
        logger.info("Starting user registration for email: {}", registerRequest.getEmail());

        if(emailExistent(registerRequest.getEmail()).isEmpty())  {
            logger.debug("Email {} is available, proceeding with registration", registerRequest.getEmail());
            User user = new User();
            user.setName(registerRequest.getName());
            user.setEmail(registerRequest.getEmail());
            user.setRole(Role.USER);
            user.setPassword(passwordEncode.encode(registerRequest.getPassword()));
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