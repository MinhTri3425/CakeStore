// CakeStore/src/main/java/com/cakestore/cakestore/controller/ChatController.java
package com.cakestore.cakestore.controller;

import com.cakestore.cakestore.websocket.ChatMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;

@Controller
public class ChatController {

    private final SimpMessageSendingOperations messagingTemplate;

    // Inject SimpMessageSendingOperations để gửi message đến broker/client
    public ChatController(SimpMessageSendingOperations messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @GetMapping("/staff/chat")
    public String staffChat() {
        return "staff/chat"; 
    }
    
    // Xử lý message gửi đến endpoint /app/chat.public (từ client)
    @MessageMapping("/chat.public")
    public void sendPublicMessage(@Payload ChatMessage chatMessage, 
                                  @AuthenticationPrincipal UserDetails userDetails) {
        // Gán thông tin người gửi và thời gian
        chatMessage.setSender(userDetails.getUsername());
        chatMessage.setSentAt(LocalDateTime.now());
        
        // Gửi lại message đến tất cả những người subscribe /topic/public
        messagingTemplate.convertAndSend("/topic/public", chatMessage);
    }
    
    // Xử lý message gửi đến endpoint /app/chat.private (Chat 1-1 Staff <-> Customer)
    @MessageMapping("/chat.private")
    public void sendPrivateMessage(@Payload ChatMessage chatMessage, 
                                   @AuthenticationPrincipal UserDetails userDetails) {
        chatMessage.setSender(userDetails.getUsername());
        chatMessage.setSentAt(LocalDateTime.now());
        
        String destination = "/queue/messages"; // Queue mặc định cho user
        String receiverEmail = chatMessage.getReceiver(); 
        
        if (receiverEmail != null && !receiverEmail.isBlank()) {
            // Gửi message riêng tư đến người nhận (customer)
            messagingTemplate.convertAndSendToUser(receiverEmail, destination, chatMessage);
            
            // Gửi bản sao cho người gửi (staff) để hiển thị trên màn hình chat của họ
            messagingTemplate.convertAndSendToUser(userDetails.getUsername(), destination, chatMessage);
        }
    }
}