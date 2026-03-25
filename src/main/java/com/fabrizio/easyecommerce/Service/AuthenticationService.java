package com.fabrizio.easyecommerce.Service;

import com.fabrizio.easyecommerce.dto.AuthenticationRequest;
import com.fabrizio.easyecommerce.dto.AuthenticationResponse;
import com.fabrizio.easyecommerce.dto.UserDTO;
import com.fabrizio.easyecommerce.entity.User;
import com.fabrizio.easyecommerce.enums.Role;
import com.fabrizio.easyecommerce.mapper.UserMapper;
import com.fabrizio.easyecommerce.repository.UserRepository;
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


    public void register(UserDTO userDTO){

        if(emailExistent(userDTO.getEmail()).isEmpty())  {
            User user = userMapper.toUser(userDTO);
            user.setRole(Role.USER);
            user.setPassword(passwordEncode.encode(user.getPassword()));
            userRepository.save(user);
        }else{
            throw new RuntimeException("Email already exists");
        }

    }


    public AuthenticationResponse login(AuthenticationRequest request){
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                request.getEmail(), request.getPassword()
        );
        authenticationManager.authenticate(authToken);

        User user = userRepository.findByEmail(request.getEmail()).get();

        String jwt = jwtService.generateToken(user.getEmail(), generateExtraClaims(user));
        return new AuthenticationResponse(jwt);
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
