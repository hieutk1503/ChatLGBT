package com.hieu.chat.controller;

import com.hieu.chat.entity.User;
import com.hieu.chat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    // API 1: Lấy thông tin cá nhân (để lúc Login tải được avatar cũ về)
    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        return ResponseEntity.ok(user); // Trả về thông tin user có chứa avatarUrl
    }

    // API 2: Upload Avatar
    @PostMapping("/avatar")
    public ResponseEntity<?> uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findByEmail(email).orElseThrow();

            // 1. Tạo thư mục chứa ảnh nếu chưa có
            String uploadDir = "uploads/avatars/";
            File directory = new File(uploadDir);
            if (!directory.exists()) directory.mkdirs();

            // 2. Tạo tên file mới ngẫu nhiên (tránh trùng lặp tên)
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFilename = UUID.randomUUID().toString() + extension;

            // 3. Lưu file xuống ổ cứng
            Path filePath = Paths.get(uploadDir + newFilename);
            Files.write(filePath, file.getBytes());

            // 4. Cập nhật đường dẫn vào DB
            String avatarUrl = "/uploads/avatars/" + newFilename;
            user.setAvatarUrl(avatarUrl);
            userRepository.save(user);

            // Trả URL về cho Frontend hiển thị ngay lập tức
            return ResponseEntity.ok(avatarUrl);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi upload: " + e.getMessage());
        }
    }
}