import { DatePipe, TitleCasePipe, formatDate } from '@angular/common';
import { Component, OnDestroy, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Observable, Subscription, forkJoin, switchMap, tap } from 'rxjs';
import { errorMessage, fieldErrorsOf } from '../core/http-error';
import { Appointment } from '../models/appointment.model';
import { Consultation, ConsultationDetails } from '../models/consultation.model';
import { Patient } from '../models/patient.model';
import { AppointmentService } from '../services/appointment.service';
import { ConsultationService } from '../services/consultation.service';
import { PatientService } from '../services/patient.service';
import { StatusBadgeComponent } from '../shared/status-badge.component';

const BP_PATTERN = /^\d{2,3}\/\d{2,3}$/;
/** Same rule as the backend: a consultation needs this many vitals before it can be completed. */
const MIN_VITALS = 2;

@Component({
  selector: 'app-consultation',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, DatePipe, TitleCasePipe, StatusBadgeComponent],
  templateUrl: './consultation.component.html',
})
export class ConsultationComponent implements OnInit, OnDestroy {
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly patientService = inject(PatientService);
  private readonly appointmentService = inject(AppointmentService);
  private readonly consultationService = inject(ConsultationService);
  private readonly subscriptions = new Subscription();

  readonly minVitals = MIN_VITALS;

  readonly form = this.fb.nonNullable.group({
    bloodPressure: ['', [Validators.pattern(BP_PATTERN)]],
    temperature: this.fb.control<number | null>(null, [Validators.min(30), Validators.max(110)]),
    notes: ['', [Validators.maxLength(1000)]],
  });

  // ----- consultation form state -----
  appointmentId: number | null = null;
  appointment: Appointment | null = null;
  consultation: Consultation | null = null;
  formLoading = false;
  formError = '';
  formSuccess = '';
  saving = false;
  serverFieldErrors: Record<string, string> = {};

  // ----- appointment picker (shown when no appointment is open) -----
  openAppointments: Appointment[] = [];
  pickerLoading = false;
  pickerError = '';

  // ----- history state -----
  patients: Patient[] = [];
  historyPatientId: number | null = null;
  historyPatient: Patient | null = null;
  history: Consultation[] = [];
  historyLoading = false;
  historyError = '';

  ngOnInit(): void {
    this.patientService.list().subscribe({
      next: (list) => (this.patients = list),
      error: (err) => (this.historyError = errorMessage(err)),
    });

    this.subscriptions.add(
      this.route.paramMap.subscribe((params) => {
        const id = Number(params.get('appointmentId'));
        if (id) {
          this.openAppointment(id);
        } else {
          this.closeForm();
        }
      }),
    );

    // Arriving from the Patients screen ("History" button) shows that patient's history.
    this.subscriptions.add(
      this.route.queryParamMap.subscribe((params) => {
        const patientId = Number(params.get('patientId'));
        if (patientId) {
          this.showHistory(patientId);
        }
      }),
    );
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  // ======================= consultation form =======================

  get isCompleted(): boolean {
    return this.consultation?.status === 'COMPLETED' || this.appointment?.status === 'COMPLETED';
  }

  /** How many valid vitals are currently filled in. */
  get vitalsCount(): number {
    const bp = this.form.controls.bloodPressure;
    const temp = this.form.controls.temperature;
    let count = 0;
    if ((bp.value ?? '').trim() !== '' && bp.valid) count++;
    if (temp.value !== null && temp.valid) count++;
    return count;
  }

  get canComplete(): boolean {
    return !this.isCompleted && !this.saving && this.form.valid && this.vitalsCount >= MIN_VITALS;
  }

  private openAppointment(id: number): void {
    this.appointmentId = id;
    this.appointment = null;
    this.consultation = null;
    this.formError = '';
    this.formSuccess = '';
    this.serverFieldErrors = {};
    this.formLoading = true;
    this.form.reset({ bloodPressure: '', temperature: null, notes: '' });
    this.form.enable();

    forkJoin({
      appointment: this.appointmentService.get(id),
      consultation: this.consultationService.getByAppointment(id),
    }).subscribe({
      next: ({ appointment, consultation }) => {
        this.appointment = appointment;
        this.consultation = consultation;
        if (consultation) {
          this.form.patchValue({
            bloodPressure: consultation.bloodPressure ?? '',
            temperature: consultation.temperature,
            notes: consultation.notes ?? '',
          });
        }
        if (consultation?.status === 'COMPLETED' || appointment.status === 'COMPLETED') {
          this.form.disable();
        }
        this.formLoading = false;
        this.showHistory(appointment.patientId);
      },
      error: (err) => {
        this.formError = errorMessage(err);
        this.formLoading = false;
      },
    });
  }

  private closeForm(): void {
    this.appointmentId = null;
    this.appointment = null;
    this.consultation = null;
    this.formError = '';
    this.formSuccess = '';
    this.loadPicker();
  }

  private currentDetails(): ConsultationDetails {
    const value = this.form.getRawValue();
    const bp = (value.bloodPressure ?? '').trim();
    const notes = (value.notes ?? '').trim();
    return {
      bloodPressure: bp === '' ? null : bp,
      temperature: value.temperature,
      notes: notes === '' ? null : notes,
    };
  }

  saveDraft(): void {
    if (!this.appointmentId || this.isCompleted) return;
    this.form.markAllAsTouched();
    if (this.form.invalid) return;
    this.beginSave();

    const details = this.currentDetails();
    const request$: Observable<Consultation> = this.consultation
      ? this.consultationService.saveDraft(this.consultation.id, details)
      : this.consultationService.create({ appointmentId: this.appointmentId, ...details });

    request$.subscribe({
      next: (saved) => {
        this.consultation = saved;
        this.markAppointmentInProgress();
        this.saving = false;
        this.formSuccess = 'Draft saved. You can complete it when two vitals are entered.';
      },
      error: (err) => this.failSave(err),
    });
  }

  complete(): void {
    if (!this.appointmentId || !this.canComplete) return;
    this.beginSave();

    const details = this.currentDetails();
    const appointmentId = this.appointmentId;
    const complete$: Observable<Consultation> = this.consultation
      ? this.consultationService.complete(this.consultation.id, details)
      : this.consultationService.create({ appointmentId, ...details }).pipe(
          tap((created) => (this.consultation = created)),
          switchMap((created) => this.consultationService.complete(created.id, details)),
        );

    complete$.subscribe({
      next: (done) => {
        this.consultation = done;
        if (this.appointment) {
          this.appointment = { ...this.appointment, status: 'COMPLETED' };
        }
        this.form.disable();
        this.saving = false;
        this.formSuccess = `Consultation completed. ${done.patientName}’s appointment is now marked Completed.`;
        this.showHistory(done.patientId);
      },
      error: (err) => this.failSave(err),
    });
  }

  private beginSave(): void {
    this.saving = true;
    this.formError = '';
    this.formSuccess = '';
    this.serverFieldErrors = {};
  }

  private failSave(err: unknown): void {
    this.saving = false;
    this.formError = errorMessage(err);
    this.serverFieldErrors = fieldErrorsOf(err);
  }

  private markAppointmentInProgress(): void {
    if (this.appointment && this.appointment.status === 'SCHEDULED') {
      this.appointment = { ...this.appointment, status: 'IN_PROGRESS' };
    }
  }

  fieldError(field: 'bloodPressure' | 'temperature' | 'notes'): string {
    const control = this.form.controls[field];
    if (control.touched && control.errors) {
      switch (field) {
        case 'bloodPressure':
          return 'Use the format 120/80.';
        case 'temperature':
          return 'Enter a value between 30 and 110.';
        case 'notes':
          return 'Notes must be 1000 characters or fewer.';
      }
    }
    return this.serverFieldErrors[field] ?? '';
  }

  // ======================= appointment picker =======================

  private loadPicker(): void {
    this.pickerLoading = true;
    this.pickerError = '';
    this.appointmentService.list().subscribe({
      next: (list) => {
        this.openAppointments = list.filter((a) => a.status !== 'COMPLETED');
        this.pickerLoading = false;
      },
      error: (err) => {
        this.pickerError = errorMessage(err);
        this.pickerLoading = false;
      },
    });
  }

  optionLabel(a: Appointment): string {
    const when = formatDate(a.appointmentDateTime, 'dd MMM, hh:mm a', 'en-US');
    return `${a.patientName} · ${a.doctorName} · ${when}`;
  }

  onPickAppointment(event: Event): void {
    const raw = (event.target as HTMLSelectElement).value;
    if (raw) {
      this.router.navigate(['/consultation', Number(raw)]);
    }
  }

  // ======================= history =======================

  onHistoryPatientChange(event: Event): void {
    const raw = (event.target as HTMLSelectElement).value;
    if (raw) {
      this.showHistory(Number(raw));
    } else {
      this.historyPatientId = null;
      this.historyPatient = null;
      this.history = [];
    }
  }

  private showHistory(patientId: number): void {
    this.historyPatientId = patientId;
    this.historyLoading = true;
    this.historyError = '';

    forkJoin({
      patient: this.patientService.get(patientId),
      history: this.consultationService.historyForPatient(patientId),
    }).subscribe({
      next: ({ patient, history }) => {
        // Ignore a slow answer if the user already picked another patient.
        if (this.historyPatientId !== patientId) return;
        this.historyPatient = patient;
        this.history = history;
        this.historyLoading = false;
      },
      error: (err) => {
        if (this.historyPatientId !== patientId) return;
        this.historyError = errorMessage(err);
        this.historyLoading = false;
      },
    });
  }
}
