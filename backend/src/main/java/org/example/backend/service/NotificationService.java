package org.example.backend.service;

import org.example.backend.domain.entity.Notification;
import org.example.backend.domain.entity.User;
import org.example.backend.domain.enums.NotificationType;
import org.example.backend.repository.NotificationRepository;
import org.example.backend.repository.UserRepository;
import org.example.backend.web.dto.notification.NotificationDto;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    public void notifyUser(Long userId, String title, String message, NotificationType type) {
        User user = userRepository.findById(userId).orElseThrow();
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notificationRepository.save(notification);
    }

    public List<NotificationDto> listForUser(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(n -> new NotificationDto(
                        n.getId(),
                        n.getTitle(),
                        n.getMessage(),
                        n.getType().name(),
                        n.getIsRead(),
                        n.getCreatedAt()
                ))
                .toList();
    }
}
