package org.example.backend.service;

import org.example.backend.domain.entity.Message;
import org.example.backend.domain.entity.User;
import org.example.backend.repository.MessageRepository;
import org.example.backend.repository.UserRepository;
import org.example.backend.web.dto.message.MessageDto;
import org.example.backend.web.dto.message.SendMessageRequest;
import java.security.Principal;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final BadWordsFilterService badWordsFilterService;

    public MessageService(
            MessageRepository messageRepository,
            UserRepository userRepository,
            CurrentUserService currentUserService,
            BadWordsFilterService badWordsFilterService
    ) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
        this.badWordsFilterService = badWordsFilterService;
    }

    public MessageDto send(Principal principal, SendMessageRequest request) {
        User sender = currentUserService.require(principal);
        User receiver = userRepository.findById(request.toUserId()).orElseThrow();

        Message message = new Message();
        message.setFromUser(sender);
        message.setToUser(receiver);
        message.setContent(badWordsFilterService.sanitize(request.content()));
        message.setIsRead(false);

        return toDto(messageRepository.save(message));
    }

    public List<MessageDto> conversation(Principal principal, Long otherUserId) {
        User current = currentUserService.require(principal);
        return messageRepository.findConversation(current.getId(), otherUserId).stream()
                .map(this::toDto)
                .toList();
    }

    private MessageDto toDto(Message message) {
        return new MessageDto(
                message.getId(),
                message.getFromUser().getId(),
                message.getToUser().getId(),
                message.getContent(),
                message.getIsRead(),
                message.getCreatedAt()
        );
    }
}
