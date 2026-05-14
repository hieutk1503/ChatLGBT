package com.hieu.chat.controller;

import com.hieu.chat.dto.AuthRequest;
import com.hieu.chat.entity.User;
import com.hieu.chat.repository.UserRepository;
import com.hieu.chat.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 1. API Đăng ký
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthRequest.Register request) {
        // Kiểm tra xem email đã tồn tại chưa
        if(userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email đã được sử dụng!");
        }

        // Tạo user mới và băm mật khẩu
        User newUser = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .build();

        userRepository.save(newUser);
        return ResponseEntity.ok("Đăng ký tài khoản thành công!");
    }

    // 2. API Đăng nhập
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest.Login request) {
        // authenticationManager sẽ tự động đối chiếu mật khẩu bạn nhập với mật khẩu đã băm trong DB
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // Nếu qua được ải trên (không quăng lỗi), tức là đăng nhập đúng -> Tạo vé JWT
        String token = tokenProvider.generateToken(authentication.getName());

        return ResponseEntity.ok(token); // Trả về chuỗi JWT cho Client
    }
}