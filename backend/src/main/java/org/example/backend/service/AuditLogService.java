package org.example.backend.service;

import org.example.backend.domain.entity.AuditLog;
import org.example.backend.domain.entity.User;
import org.example.backend.repository.AuditLogRepository;
import org.example.backend.repository.UserRepository;
import org.example.backend.web.dto.log.AuditLogDto;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    public AuditLogService(AuditLogRepository auditLogRepository, UserRepository userRepository) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    public void log(Long actorUserId, String actionType, String entityType, String entityId, String details, String ipAddress) {
        AuditLog log = new AuditLog();
        if (actorUserId != null) {
            userRepository.findById(actorUserId).ifPresent(log::setActor);
        }
        log.setActionType(actionType);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setDetails(details);
        log.setIpAddress(ipAddress);
        auditLogRepository.save(log);
    }

    public List<AuditLogDto> listRecent() {
        return auditLogRepository.findTop100ByOrderByCreatedAtDesc()
                .stream()
                .map(log -> new AuditLogDto(
                        log.getId(),
                        log.getActor() == null ? "SYSTEM" : log.getActor().getEmail(),
                        log.getActionType(),
                        log.getEntityType(),
                        log.getEntityId(),
                        log.getDetails(),
                        log.getIpAddress(),
                        log.getCreatedAt()
                ))
                .toList();
    }
}
