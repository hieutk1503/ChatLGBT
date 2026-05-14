package com.hieu.chat.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "conversations")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Conversation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(nullable = false)
    private boolean isGroup;

    @CreationTimestamp
    private LocalDateTime createdAt;

    // ========== THÊM MỚI ĐOẠN NÀY ĐỂ LÀM CHAT NHÓM ==========
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "conversation_members", // Bảng trung gian sẽ tự động được sinh ra trong MySQL
            joinColumns = @JoinColumn(name = "conversation_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @JsonIgnore // "Lá chắn" chống lỗi vòng lặp vô tận khi ép kiểu sang JSON
    private List<User> members = new ArrayList<>();
}