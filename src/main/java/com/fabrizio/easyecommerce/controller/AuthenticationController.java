package com.fabrizio.easyecommerce.controller;

import com.fabrizio.easyecommerce.Service.AuthenticationService;
import com.fabrizio.easyecommerce.dto.AuthenticationRequest;
import com.fabrizio.easyecommerce.dto.AuthenticationResponse;
import com.fabrizio.easyecommerce.dto.RegisterRequest;
import com.fabrizio.easyecommerce.dto.UserDTO;
import com.fabrizio.easyecommerce.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(
        name = "Authentication",
        description = """
    Endpoints responsible for user registration, authentication, and session validation.

    🔐 Authentication Flow:
    1. Login using /login
    2. Copy the JWT token from the response
    3. Use it in Swagger or Postman:
       Authorization: Bearer <your_token>
    4. Access protected endpoints

    👤 Preloaded Users (from import.sql):

    ADMINISTRATOR:
    email: admin@test.com
    password: admin123

    USER:
    email: user@test.com
    password: user123

    ⚠️ Notes:
    - Administrator has full permissions
    - User has limited permissions depending on roles
    - All secured endpoints require a valid JWT token
    """
)
public class AuthenticationController {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);

    @Autowired
    private AuthenticationService authenticationService;

    @Operation(
            summary = "Register a new user",
            description = """
        Creates a new user in the system.

        📌 This endpoint does NOT require authentication.

        ✔ Required fields:
        - name
        - email
        - password

        ✔ Example:
        {
          "name": "John Doe",
          "email": "john@test.com",
          "password": "123456"
        }
        """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error")
    })
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest request){
        logger.info("Attempting to register new user with email: {}", request.getEmail());
        try {
            authenticationService.register(request);
            logger.info("User registered successfully: {}", request.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
        } catch (Exception e) {
            logger.error("Failed to register user: {}", request.getEmail(), e);
            throw e;
        }
    }

    @Operation(
            summary = "Login and obtain JWT token",
            description = """
        Authenticates a user and returns a JWT token.

        📌 Use one of the predefined users:

        ADMIN:
        email: admin@test.com
        password: admin123

        USER:
        email: user@test.com
        password: user123

        ✔ Example request:
        {
          "email": "admin@test.com",
          "password": "admin123"
        }

        ✔ Response:
        {
          "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
        }
        """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authentication successful"),
            @ApiResponse(responseCode = "403", description = "Invalid credentials or access denied")
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid AuthenticationRequest authRequest){
        logger.info("Login attempt for user: {}", authRequest.getEmail());
        try {
            AuthenticationResponse authenticationResponse = authenticationService.login(authRequest);
            logger.info("Login successful for user: {}", authRequest.getEmail());
            return ResponseEntity.status(HttpStatus.OK).body(authenticationResponse);
        } catch (Exception e) {
            logger.warn("Login failed for user: {}", authRequest.getEmail(), e);
            throw e;
        }
    }

    @Operation(
            summary = "Get authenticated user info",
            description = """
        Returns the information of the currently authenticated user.

        🔐 Requires JWT token.

        ✔ Header:
        Authorization: Bearer <token>

        ✔ Response example:
        {
          "id": 1,
          "name": "testAdministrator",
          "email": "admin@test.com",
          "role": "ADMINISTRATOR"
        }
        """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User authenticated"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing token")
    })
    @GetMapping("/auth-me")
    public ResponseEntity<UserDTO> authMe() {
        logger.debug("Auth-me request received");
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof User user) {
            logger.info("Auth-me successful for user: {}", user.getEmail());
            UserDTO userDTO = new UserDTO();
            userDTO.setId(user.getId());
            userDTO.setName(user.getName());
            userDTO.setEmail(user.getEmail());
            userDTO.setRole(user.getRole());

            return ResponseEntity.status(HttpStatus.OK).body(userDTO);
        }

        logger.warn("Auth-me failed: no authenticated user found");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}