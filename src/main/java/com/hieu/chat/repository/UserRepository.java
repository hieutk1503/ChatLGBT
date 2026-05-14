package com.hieu.chat.repository;

import com.hieu.chat.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Tìm 1 User (Dùng cho đăng nhập, chat 1-1)
    Optional<User> findByEmail(String email);


    // Tìm nhiều User cùng lúc dựa vào danh sách Email (Dùng để gom thành viên vào Nhóm)
    List<User> findByEmailIn(List<String> emails);
}