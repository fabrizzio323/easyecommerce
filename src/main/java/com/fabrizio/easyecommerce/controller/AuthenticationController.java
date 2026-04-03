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

    🔐 **Authentication Flow**:
    1. Register with shipping details using `/register`
    2. Login using `/login`
    3. Use the JWT token in headers: `Authorization: Bearer <token>`

    👤 **Preloaded Users (from import.sql)**:
    - **ADMIN**: admin@test.com / admin123 (Location: Av. Fascio 123, San Salvador de Jujuy)
    - **USER**: user@test.com / user123 (Location: Belgrano 450, San Salvador de Jujuy)
    """
)
public class AuthenticationController {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);

    @Autowired
    private AuthenticationService authenticationService;

    @Operation(
            summary = "Register a new user with shipping address",
            description = """
        Creates a new user in the system including mandatory shipping information.
        
        📌 **Public Endpoint**
        
        ✔ **Required fields**:
        - **name**: Full name
        - **email**: Valid and unique email
        - **password**: Minimum 6 characters
        - **street**: Street name and number
        - **city**: City (e.g., San Salvador de Jujuy)
        - **zipCode**: Postal code (e.g., 4600)

        ✔ **Example**:
        {
          "name": "Fabrizio Armada",
          "email": "fabri@test.com",
          "password": "password123",
          "street": "Alvear 1020",
          "city": "San Salvador de Jujuy",
          "zipCode": "4600"
        }
        """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error - Missing fields or invalid format")
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
        
        ✔ **Example request**:
        {
          "email": "admin@test.com",
          "password": "admin123"
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
            summary = "Get authenticated user info including address",
            description = """
        Returns the profile information of the currently authenticated user.

        🔐 **Requires JWT token**
        
        ✔ **Response example**:
        {
          "id": 1,
          "name": "Fabrizio Armada",
          "email": "admin@test.com",
          "role": "ADMINISTRATOR",
          "street": "Av. Fascio 123",
          "city": "San Salvador de Jujuy",
          "zipCode": "4600"
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
            userDTO.setStreet(user.getStreet());
            userDTO.setCity(user.getCity());
            userDTO.setZipCode(user.getZipCode());

            return ResponseEntity.status(HttpStatus.OK).body(userDTO);
        }

        logger.warn("Auth-me failed: no authenticated user found");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}