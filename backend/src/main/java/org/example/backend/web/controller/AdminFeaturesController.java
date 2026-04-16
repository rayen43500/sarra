package org.example.backend.web.controller;

import org.example.backend.domain.enums.AppointmentStatus;
import org.example.backend.service.AppointmentService;
import org.example.backend.web.dto.appointment.AppointmentDto;
import java.util.List;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/features")
public class AdminFeaturesController {

    private final AppointmentService appointmentService;
    private final JobLauncher jobLauncher;
    private final Job newsletterDigestJob;

    public AdminFeaturesController(AppointmentService appointmentService, JobLauncher jobLauncher, Job newsletterDigestJob) {
        this.appointmentService = appointmentService;
        this.jobLauncher = jobLauncher;
        this.newsletterDigestJob = newsletterDigestJob;
    }

    @GetMapping("/appointments")
    public ResponseEntity<List<AppointmentDto>> allAppointments() {
        return ResponseEntity.ok(appointmentService.all());
    }

    @PatchMapping("/appointments/{id}/status")
    public ResponseEntity<AppointmentDto> updateAppointmentStatus(@PathVariable Long id, @RequestParam AppointmentStatus status) {
        return ResponseEntity.ok(appointmentService.updateStatus(id, status));
    }

    @PostMapping("/batch/newsletter-digest/run")
    public ResponseEntity<String> runNewsletterBatch() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addLong("ts", System.currentTimeMillis())
                .toJobParameters();
        jobLauncher.run(newsletterDigestJob, params);
        return ResponseEntity.ok("Batch newsletterDigestJob launched");
    }
}
