package org.example.backend.web.controller;

import org.example.backend.web.dto.chat.ChatMessage;
import java.time.Instant;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class ChatWebSocketController {

    @MessageMapping("/chat.send")
    @SendTo("/topic/public")
    public ChatMessage send(@Payload ChatMessage message) {
        return new ChatMessage(message.sender(), message.content(), Instant.now().toString());
    }
}
