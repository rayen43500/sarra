package org.example.backend.service;

import org.example.backend.domain.entity.Appointment;
import org.example.backend.domain.entity.User;
import org.example.backend.domain.enums.AppointmentStatus;
import org.example.backend.repository.AppointmentRepository;
import org.example.backend.web.dto.appointment.AppointmentDto;
import org.example.backend.web.dto.appointment.AppointmentRequest;
import java.security.Principal;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final CurrentUserService currentUserService;
    private final BadWordsFilterService badWordsFilterService;

    public AppointmentService(
            AppointmentRepository appointmentRepository,
            CurrentUserService currentUserService,
            BadWordsFilterService badWordsFilterService
    ) {
        this.appointmentRepository = appointmentRepository;
        this.currentUserService = currentUserService;
        this.badWordsFilterService = badWordsFilterService;
    }

    public AppointmentDto create(Principal principal, AppointmentRequest request) {
        User user = currentUserService.require(principal);

        Appointment appointment = new Appointment();
        appointment.setUser(user);
        appointment.setScheduledAt(request.scheduledAt());
        appointment.setNote(badWordsFilterService.sanitize(request.note()));
        appointment.setStatus(AppointmentStatus.REQUESTED);

        return toDto(appointmentRepository.save(appointment));
    }

    public List<AppointmentDto> mine(Principal principal) {
        User user = currentUserService.require(principal);
        return appointmentRepository.findByUserIdOrderByScheduledAtDesc(user.getId())
                .stream()
                .map(this::toDto)
                .toList();
    }

    public List<AppointmentDto> all() {
        return appointmentRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    public AppointmentDto updateStatus(Long id, AppointmentStatus status) {
        Appointment appointment = appointmentRepository.findById(id).orElseThrow();
        appointment.setStatus(status);
        return toDto(appointmentRepository.save(appointment));
    }

    public int markMissedAppointments() {
        List<Appointment> missed = appointmentRepository.findByStatusInAndScheduledAtBefore(
                List.of(AppointmentStatus.REQUESTED, AppointmentStatus.CONFIRMED),
                java.time.LocalDateTime.now()
        );
        missed.forEach(a -> a.setStatus(AppointmentStatus.MISSED));
        appointmentRepository.saveAll(missed);
        return missed.size();
    }

    private AppointmentDto toDto(Appointment appointment) {
        return new AppointmentDto(
                appointment.getId(),
                appointment.getUser().getId(),
                appointment.getScheduledAt(),
                appointment.getNote(),
                appointment.getStatus().name(),
                appointment.getCreatedAt()
        );
    }
}
