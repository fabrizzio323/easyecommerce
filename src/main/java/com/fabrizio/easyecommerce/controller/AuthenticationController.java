package com.fabrizio.easyecommerce.controller;

import com.fabrizio.easyecommerce.Service.AuthenticationService;
import com.fabrizio.easyecommerce.dto.AuthenticationRequest;
import com.fabrizio.easyecommerce.dto.AuthenticationResponse;
import com.fabrizio.easyecommerce.dto.RegisterRequest;
import com.fabrizio.easyecommerce.dto.UserDTO;
import com.fabrizio.easyecommerce.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication resource", description = "Endpoints for user registration, login, and authentication status")
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;

    @Operation(summary="Register a new user given a user object")
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest request){
            authenticationService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
    }

    @Operation(summary="Login a user given an email and password, returns a JWT token if successful")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid AuthenticationRequest authRequest){
            AuthenticationResponse authenticationResponse = authenticationService.login(authRequest);
            return ResponseEntity.status(HttpStatus.OK).body(authenticationResponse);
    }

    @Operation(summary="Get the authenticated user's information, requires a valid JWT token in the Authorization header")
    @GetMapping("/auth-me")
    public ResponseEntity<UserDTO> authMe() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User user) {
            UserDTO userDTO = new UserDTO();
            userDTO.setId(user.getId());
            userDTO.setName(user.getName());
            userDTO.setEmail(user.getEmail());
            userDTO.setRole(user.getRole());
            return ResponseEntity.status(HttpStatus.OK).body(userDTO);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}