// CakeStore/src/main/java/com/cakestore/cakestore/websocket/ChatMessage.java (CHỈNH SỬA)
package com.cakestore.cakestore.websocket;

import java.time.LocalDateTime;

public class ChatMessage {
    private String sender;
    private String receiver; // Email hoặc ID của người nhận (cho private chat)
    private String content;
    private LocalDateTime sentAt;

    // Constructors
    public ChatMessage() {}

    public ChatMessage(String sender, String receiver, String content) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.sentAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
}