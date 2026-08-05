package com.example.taskmanager.controller;

import com.example.taskmanager.Security.JwtUtil;
import com.example.taskmanager.exception.ValidationException;
import com.example.taskmanager.model.Role;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;
/*ResponseEntity what is happening -> When it encounters a Map<String, String>,
it converts the map's key-value pairs directly into standard JSON format ({"key": "value"}
*/

@RestController
public class RegisterController {
        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtUtil jwtUtil;                       // final forces you to set it

        public RegisterController(UserRepository userRepository,
                                  PasswordEncoder passwordEncoder,
                                  JwtUtil jwtUtil) {         // Spring injects it here
            this.userRepository = userRepository;
            this.passwordEncoder = passwordEncoder;
            this.jwtUtil = jwtUtil;                          // ← the missing assignment
        }

    @PostMapping("/register") //return ResponseEntity -> Spring automatically constructs and customizes a complete HTTP response where the payload body is serialized into a flat JSON object.
    public ResponseEntity<Map<String, String>> register(@RequestBody User request) {
        //  validate null / blank
        if (request.getUsername() == null || request.getUsername().isBlank() ||
                request.getPassword() == null || request.getPassword().length() < 6) {
            throw new ValidationException("Invalid username or password format");
        }
        // validate uniqueness -> reject duplicate usernames
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ValidationException("Username is already taken");
        }
        //  Hashing password, then save -> the one place plain text is turned into a hash
        String hashed = passwordEncoder.encode(request.getPassword());
        userRepository.save(new User(request.getUsername(), hashed, Role.USER));
        String token = jwtUtil.generateToken(request);
        return ResponseEntity.status(HttpStatus.CREATED)//do i need this for the front end?
                .body(Map.of(
                        "message", "Account created",
                        "token", token));    }
}

