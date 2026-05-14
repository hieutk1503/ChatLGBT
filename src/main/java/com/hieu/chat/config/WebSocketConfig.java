package com.hieu.chat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker // Kích hoạt tính năng Broker (Trạm trung chuyển tin nhắn)
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Mở một endpoint tên là /ws để Client kết nối vào
        // setAllowedOriginPatterns("*") cho phép mọi ứng dụng (React, Postman...) kết nối
        // withSockJS() là cơ chế dự phòng nếu trình duyệt cũ không hỗ trợ WebSocket thuần
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Định tuyến tin nhắn:
        // Những tin nhắn nào bắt đầu bằng /app thì sẽ gửi vào Controller để xử lý (Code của mình)
        registry.setApplicationDestinationPrefixes("/app");

        // Những tin nhắn nào bắt đầu bằng /topic hoặc /user thì Broker sẽ tự động đẩy thẳng về cho Client
        registry.enableSimpleBroker("/topic", "/user");

        // Tiền tố dành riêng cho việc gửi tin nhắn riêng tư (1-1)
        registry.setUserDestinationPrefix("/user");
    }
}