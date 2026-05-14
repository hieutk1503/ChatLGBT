package com.hieu.chat.service;

import com.hieu.chat.entity.Friendship;
import com.hieu.chat.entity.User;
import com.hieu.chat.repository.FriendshipRepository;
import com.hieu.chat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    // 1. Hàm gửi lời mời kết bạn (Đã sửa logic chặn 2 chiều)
    public String sendFriendRequest(String requesterEmail, Long receiverId) {
        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new RuntimeException("Lỗi: Không tìm thấy người gửi"));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Lỗi: Không tìm thấy người nhận"));

        if (requester.getId().equals(receiver.getId())) {
            throw new RuntimeException("Bạn không thể tự kết bạn với chính mình!");
        }

        // KIỂM TRA QUAN HỆ TỔNG THỂ (Bất kể ai gửi ai nhận)
        Optional<Friendship> existing = friendshipRepository.findExistingFriendship(requester, receiver);

        if (existing.isPresent()) {
            Friendship f = existing.get();

            // Trường hợp 1: Đã là bạn bè rồi
            if ("ACCEPTED".equals(f.getStatus())) {
                throw new RuntimeException("Hai người đã là bạn bè của nhau rồi!");
            }

            // Trường hợp 2: Đang chờ duyệt (PENDING)
            if ("PENDING".equals(f.getStatus())) {
                if (f.getRequester().getId().equals(requester.getId())) {
                    throw new RuntimeException("Bạn đã gửi lời mời cho người này rồi, vui lòng chờ!");
                } else {
                    throw new RuntimeException("Người này đã gửi lời mời cho bạn từ trước, hãy vào mục chờ duyệt để chấp nhận!");
                }
            }
        }

        // Nếu chưa có quan hệ gì thì mới cho phép tạo bản ghi mới
        Friendship request = Friendship.builder()
                .requester(requester)
                .receiver(receiver)
                .status("PENDING") // Trạng thái: Đang chờ duyệt
                .build();

        friendshipRepository.save(request);
        return "Đã gửi lời mời kết bạn tới: " + receiver.getFullName();
    }

    // 2. Hàm chấp nhận kết bạn (Giữ nguyên)
    public String acceptFriendRequest(Long friendshipId, String receiverEmail) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lời mời này"));

        // Xác minh xem người bấm "Chấp nhận" có đúng là người được nhận lời mời không
        if (!friendship.getReceiver().getEmail().equals(receiverEmail)) {
            throw new RuntimeException("Bạn không có quyền duyệt lời mời này!");
        }

        friendship.setStatus("ACCEPTED");
        friendshipRepository.save(friendship);
        return "Bạn và " + friendship.getRequester().getFullName() + " đã trở thành bạn bè!";
    }
    // 3. Hàm lấy danh sách bạn bè
    public java.util.List<User> getMyFriends(String myEmail) {
        User me = userRepository.findByEmail(myEmail)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        java.util.List<Friendship> friendships = friendshipRepository.findAcceptedFriendships(me);

        // Bóc tách danh sách: Lọc ra người kia (không phải mình)
        return friendships.stream()
                .map(f -> f.getRequester().getId().equals(me.getId()) ? f.getReceiver() : f.getRequester())
                .distinct()
                .toList();
    }
    public List<Friendship> getPendingRequests(String receiverEmail) {
        // 1. Tìm người dùng hiện tại (người đang chờ được kết bạn)
        User receiver = userRepository.findByEmail(receiverEmail)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản người dùng!"));

        // 2. Lấy danh sách các lời mời có trạng thái là "PENDING" (Đang chờ)
        // Nếu database của bạn lưu trạng thái là số (0) hay Enum, hãy thay chữ "PENDING" cho khớp nhé!
        return friendshipRepository.findByReceiverAndStatus(receiver, "PENDING");
    }
}