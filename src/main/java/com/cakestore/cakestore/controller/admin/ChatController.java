// src/main/java/com/cakestore/cakestore/controller/ChatController.java
package com.cakestore.cakestore.controller.admin;

import com.cakestore.cakestore.entity.User;
import com.cakestore.cakestore.service.admin.UserService;
import com.cakestore.cakestore.websocket.ChatMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.core.userdetails.UserDetails; // Import UserDetails
import org.springframework.security.core.annotation.AuthenticationPrincipal; // Import AuthenticationPrincipal
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal; // Import Principal
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ChatController {

    private final SimpMessageSendingOperations messagingTemplate;
    private final UserService userService;

    public ChatController(SimpMessageSendingOperations messagingTemplate, UserService userService) {
        this.messagingTemplate = messagingTemplate;
        this.userService = userService;
    }

    @GetMapping("/admin/chat")
    // Sử dụng @AuthenticationPrincipal UserDetails ở đây để thuận tiện lấy username ban đầu
    public String showChatPage(Model model, @AuthenticationPrincipal UserDetails currentUserDetails) {
        List<User> internalUsers = userService.findPotentialManagers();

        String currentUserEmail = currentUserDetails.getUsername();
        List<User> otherInternalUsers = internalUsers.stream()
                .filter(user -> !user.getEmail().equals(currentUserEmail))
                .collect(Collectors.toList());

        model.addAttribute("internalUsers", otherInternalUsers);
        model.addAttribute("defaultReceiver", otherInternalUsers.isEmpty() ? null : otherInternalUsers.get(0).getEmail());
        // Thêm currentUserEmail vào model để JavaScript biết mình là ai
        model.addAttribute("currentUserEmail", currentUserEmail);
        return "common/chat";
    }

    @MessageMapping("/chat.public")
    // Sử dụng Principal để tránh lỗi Deserialization khi nhận message
    public void sendPublicMessage(@Payload ChatMessage chatMessage, Principal principal) {
        chatMessage.setSender(principal.getName());
        chatMessage.setSentAt(LocalDateTime.now());
        messagingTemplate.convertAndSend("/topic/public", chatMessage);
    }

    @MessageMapping("/chat.private")
    // Sử dụng Principal để tránh lỗi Deserialization khi nhận message
    public void sendPrivateMessage(@Payload ChatMessage chatMessage, Principal principal) {
        String senderEmail = principal.getName();
        chatMessage.setSender(senderEmail);
        chatMessage.setSentAt(LocalDateTime.now()); // Server đặt thời gian gửi
        String destination = "/queue/messages";
        String receiverEmail = chatMessage.getReceiver();

        System.out.println("--- Processing private message ---");
        System.out.println("Sender: " + senderEmail);
        System.out.println("Receiver: " + receiverEmail);
        System.out.println("Content: " + chatMessage.getContent());

        if (receiverEmail != null && !receiverEmail.isBlank()) {
            try {
                System.out.println("Sending to receiver: " + receiverEmail);
                messagingTemplate.convertAndSendToUser(receiverEmail, destination, chatMessage);
                System.out.println("Sent to receiver successfully.");

                // Gửi lại cho người gửi (để hỗ trợ nhiều tab/thiết bị cùng login)
                if (!senderEmail.equals(receiverEmail)) {
                     System.out.println("Sending back to sender: " + senderEmail);
                     messagingTemplate.convertAndSendToUser(senderEmail, destination, chatMessage);
                     System.out.println("Sent back to sender successfully.");
                }

            } catch (Exception e) {
                System.err.println("Error sending message: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
             System.err.println("Cannot send private message, receiver is null or blank.");
        }
        System.out.println("--- Finished processing private message ---");
    }
}