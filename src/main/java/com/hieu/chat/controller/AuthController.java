package com.hieu.chat.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.hieu.chat.dto.AuthRequest;
import com.hieu.chat.entity.User;
import com.hieu.chat.repository.UserRepository;
import com.hieu.chat.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

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
    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody Map<String, String> payload) {
        try {
            String googleToken = payload.get("token");

            // 1. Xác thực Token với Google
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList("560953078248-qrsgt0dllo272cb3ianhjr3aojbvvtvg.apps.googleusercontent.com"))
                    .build();

            GoogleIdToken idToken = verifier.verify(googleToken);
            if (idToken == null) {
                return ResponseEntity.badRequest().body("Token Google không hợp lệ!");
            }

            // 2. Lấy thông tin user từ Google
            GoogleIdToken.Payload googlePayload = idToken.getPayload();
            String email = googlePayload.getEmail();
            String name = (String) googlePayload.get("name");
            String pictureUrl = (String) googlePayload.get("picture");

            // 3. Kiểm tra xem user này đã có trong DB của ZChat chưa?
            User user = userRepository.findByEmail(email).orElse(null);
            if (user == null) {
                // Nếu chưa có -> Tự động đăng ký tài khoản mới cho họ
                user = new User();
                user.setEmail(email);
                user.setFullName(name);
                user.setAvatarUrl(pictureUrl); // Lấy luôn avatar Google cho ngầu
                user.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));                userRepository.save(user);
            }

            // 4. Tạo JWT của hệ thống ZChat và trả về
            String jwt = tokenProvider.generateToken(user.getEmail());
            return ResponseEntity.ok(jwt);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi server: " + e.getMessage());
        }
    }
}