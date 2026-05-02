import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Appointment, FeatureApiService } from '../../../core/services/feature-api.service';

@Component({
  selector: 'app-client-appointments',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './client-appointments.component.html',
  styleUrl: './client-appointments.component.css'
})
export class ClientAppointmentsComponent {
  protected appointmentDateTime = '';
  protected appointmentNote = '';
  protected appointmentMessage = '';
  protected appointments: Appointment[] = [];

  constructor(private readonly featureApiService: FeatureApiService) {
    this.loadAppointments();
  }

  protected createAppointment(): void {
    if (!this.appointmentDateTime) {
      this.appointmentMessage = 'Merci de choisir une date.';
      return;
    }

    const scheduledAt = new Date(this.appointmentDateTime).toISOString();
    this.featureApiService.createAppointment(scheduledAt, this.appointmentNote).subscribe({
      next: () => {
        this.appointmentMessage = 'RDV cree avec succes.';
        this.appointmentNote = '';
        this.loadAppointments();
      },
      error: () => {
        this.appointmentMessage = 'Echec creation RDV.';
      }
    });
  }

  protected appointmentBadgeClass(status: string): string {
    if (status === 'CONFIRMED') {
      return 'success';
    }
    if (status === 'CANCELLED' || status === 'MISSED') {
      return 'danger';
    }
    return 'warning';
  }

  private loadAppointments(): void {
    this.featureApiService.myAppointments().subscribe({
      next: (appointments) => {
        this.appointments = appointments;
      }
    });
  }
}
