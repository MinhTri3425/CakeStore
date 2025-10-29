// CakeStore/src/main/java/com/cakestore/cakestore/config/WebSocketConfig.java
package com.cakestore.cakestore.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Định nghĩa prefix cho các message gửi từ server đến client (subscribe)
        config.enableSimpleBroker("/topic", "/queue");
        // Định nghĩa prefix cho các endpoint mà client gửi message đến server (send)
        config.setApplicationDestinationPrefixes("/app");
        // Đặt prefix cho user private messaging (mỗi user có queue riêng)
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Đăng ký endpoint HTTP để mở kết nối WebSocket.
        // Cấu hình CORS để cho phép kết nối từ bất kỳ domain nào (hoặc nên giới hạn)
        registry.addEndpoint("/ws").withSockJS();
    }
}