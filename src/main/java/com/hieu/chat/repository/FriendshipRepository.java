package com.hieu.chat.repository;

import com.hieu.chat.entity.Friendship;
import com.hieu.chat.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
    // Tìm kiếm bản ghi giữa user1 và user2 bất kể vai trò requester hay receiver
    @Query("SELECT f FROM Friendship f WHERE " +
            "(f.requester = :u1 AND f.receiver = :u2) OR " +
            "(f.requester = :u2 AND f.receiver = :u1)")
    Optional<Friendship> findExistingFriendship(@Param("u1") User u1, @Param("u2") User u2);
    // Lấy danh sách bạn bè đã đồng ý của một user
    @Query("SELECT f FROM Friendship f WHERE (f.requester = :user OR f.receiver = :user) AND f.status = 'ACCEPTED'")
    java.util.List<Friendship> findAcceptedFriendships(@Param("user") User user);
    List<Friendship> findByReceiverAndStatus(User receiver, String status);
}