package org.example.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class MaintenanceScheduler {

    private static final Logger log = LoggerFactory.getLogger(MaintenanceScheduler.class);

    private final AppointmentService appointmentService;

    public MaintenanceScheduler(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @Scheduled(cron = "0 */30 * * * *")
    public void markMissedAppointments() {
        int updated = appointmentService.markMissedAppointments();
        if (updated > 0) {
            log.info("[SCHEDULER] Marked {} appointments as MISSED", updated);
        }
    }
}
