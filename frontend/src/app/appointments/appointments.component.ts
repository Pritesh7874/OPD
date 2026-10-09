import { DatePipe } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { errorMessage, fieldErrorsOf } from '../core/http-error';
import { localDateString, nowForDateTimeInput } from '../core/date-utils';
import { Appointment } from '../models/appointment.model';
import { Patient } from '../models/patient.model';
import { AppointmentService } from '../services/appointment.service';
import { PatientService } from '../services/patient.service';
import { StatusBadgeComponent } from '../shared/status-badge.component';

type View = 'today' | 'all';

@Component({
  selector: 'app-appointments',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, DatePipe, StatusBadgeComponent],
  templateUrl: './appointments.component.html',
})
export class AppointmentsComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly patientService = inject(PatientService);
  private readonly appointmentService = inject(AppointmentService);

  readonly form = this.fb.nonNullable.group({
    patientId: this.fb.control<number | null>(null, [Validators.required]),
    doctorName: ['', [Validators.required, Validators.pattern(/\S/), Validators.maxLength(100)]],
    appointmentDateTime: [nowForDateTimeInput(), [Validators.required]],
  });

  patients: Patient[] = [];
  patientsLoading = true;
  patientsError = '';

  view: View = 'today';
  appointments: Appointment[] = [];
  listLoading = true;
  listError = '';

  submitted = false;
  saving = false;
  successMessage = '';
  formError = '';
  serverFieldErrors: Record<string, string> = {};

  ngOnInit(): void {
    this.loadPatients();
    this.loadAppointments();
  }

  private loadPatients(): void {
    this.patientService.list().subscribe({
      next: (list) => {
        this.patients = list;
        this.patientsLoading = false;
        // Arriving from the Patients screen ("Book" button) pre-selects that patient.
        const preselected = Number(this.route.snapshot.queryParamMap.get('patientId'));
        if (preselected && list.some((p) => p.id === preselected)) {
          this.form.controls.patientId.setValue(preselected);
        }
      },
      error: (err) => {
        this.patientsError = errorMessage(err);
        this.patientsLoading = false;
      },
    });
  }

  setView(view: View): void {
    this.view = view;
    this.loadAppointments();
  }

  loadAppointments(): void {
    this.listLoading = true;
    this.listError = '';
    const request$ = this.view === 'today' ? this.appointmentService.today() : this.appointmentService.list();
    request$.subscribe({
      next: (list) => {
        this.appointments = list;
        this.listLoading = false;
      },
      error: (err) => {
        this.listError = errorMessage(err);
        this.listLoading = false;
      },
    });
  }

  onPatientChange(event: Event): void {
    const raw = (event.target as HTMLSelectElement).value;
    this.form.controls.patientId.setValue(raw ? Number(raw) : null);
  }

  actionLabel(a: Appointment): string {
    switch (a.status) {
      case 'SCHEDULED':
        return 'Start consultation';
      case 'IN_PROGRESS':
        return 'Continue';
      default:
        return 'View summary';
    }
  }

  fieldError(field: 'patientId' | 'doctorName' | 'appointmentDateTime'): string {
    const control = this.form.controls[field];
    if ((control.touched || this.submitted) && control.errors) {
      switch (field) {
        case 'patientId':
          return 'Select a patient.';
        case 'doctorName':
          return control.errors['maxlength'] ? 'Doctor name must be 100 characters or fewer.' : 'Enter the doctor’s name.';
        case 'appointmentDateTime':
          return 'Choose a date and time.';
      }
    }
    return this.serverFieldErrors[field] ?? '';
  }

  book(): void {
    this.submitted = true;
    this.successMessage = '';
    this.formError = '';
    this.serverFieldErrors = {};

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const value = this.form.getRawValue();
    this.saving = true;
    this.appointmentService
      .book({
        patientId: value.patientId as number,
        doctorName: value.doctorName.trim(),
        appointmentDateTime: value.appointmentDateTime,
      })
      .subscribe({
        next: (appointment) => {
          this.saving = false;
          this.submitted = false;
          this.successMessage = `Appointment booked for ${appointment.patientName} with ${appointment.doctorName}.`;
          // If the slot is on another day, switch to "All" so the new row is visible.
          if (value.appointmentDateTime.slice(0, 10) !== localDateString()) {
            this.view = 'all';
          }
          this.form.reset({
            patientId: null,
            doctorName: '',
            appointmentDateTime: nowForDateTimeInput(),
          });
          this.loadAppointments();
        },
        error: (err) => {
          this.saving = false;
          this.formError = errorMessage(err);
          this.serverFieldErrors = fieldErrorsOf(err);
        },
      });
  }
}
