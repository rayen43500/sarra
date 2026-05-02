import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Appointment, FeatureApiService } from '../../../core/services/feature-api.service';

type FilterStatus = 'ALL' | 'PENDING' | 'CONFIRMED' | 'CANCELLED' | 'MISSED';

@Component({
  selector: 'app-admin-appointments',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './admin-appointments.component.html',
  styleUrl: './admin-appointments.component.css'
})
export class AdminAppointmentsComponent implements OnInit {
  appointments: Appointment[] = [];
  filtered: Appointment[] = [];
  isLoading = true;
  actionMessage = '';
  filterStatus: FilterStatus = 'ALL';
  filters: FilterStatus[] = ['ALL', 'PENDING', 'CONFIRMED', 'CANCELLED', 'MISSED'];

  constructor(private readonly featureApiService: FeatureApiService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.isLoading = true;
    this.featureApiService.adminAppointments().subscribe({
      next: (data) => {
        this.appointments = data;
        this.applyFilter();
        this.isLoading = false;
      },
      error: () => { this.isLoading = false; }
    });
  }

  applyFilter(): void {
    if (this.filterStatus === 'ALL') {
      this.filtered = [...this.appointments];
    } else {
      this.filtered = this.appointments.filter(a => a.status === this.filterStatus);
    }
  }

  setFilter(status: FilterStatus): void {
    this.filterStatus = status;
    this.applyFilter();
  }

  updateStatus(id: number, status: string): void {
    this.featureApiService.updateAppointmentStatus(id, status).subscribe({
      next: () => {
        this.actionMessage = `RDV #${id} mis à jour → ${status}`;
        this.load();
        setTimeout(() => this.actionMessage = '', 3000);
      },
      error: () => {
        this.actionMessage = '❌ Impossible de mettre à jour le statut.';
      }
    });
  }

  statusBadgeClass(status: string): string {
    const map: Record<string, string> = {
      PENDING: 'badge-warning',
      CONFIRMED: 'badge-success',
      CANCELLED: 'badge-danger',
      MISSED: 'badge-muted'
    };
    return map[status] ?? 'badge-muted';
  }

  countByStatus(status: FilterStatus): number {
    if (status === 'ALL') return this.appointments.length;
    return this.appointments.filter(a => a.status === status).length;
  }
}
