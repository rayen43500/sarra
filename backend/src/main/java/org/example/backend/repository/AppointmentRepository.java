package org.example.backend.repository;

import org.example.backend.domain.entity.Appointment;
import org.example.backend.domain.enums.AppointmentStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByUserIdOrderByScheduledAtDesc(Long userId);
    List<Appointment> findByStatusInAndScheduledAtBefore(List<AppointmentStatus> statuses, LocalDateTime before);
}
