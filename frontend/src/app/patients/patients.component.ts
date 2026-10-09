import { TitleCasePipe } from '@angular/common';
import { Component, OnDestroy, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Subject, Subscription, catchError, debounceTime, of, switchMap, tap } from 'rxjs';
import { errorMessage, fieldErrorsOf } from '../core/http-error';
import { Gender, Patient } from '../models/patient.model';
import { PatientService } from '../services/patient.service';

const PHONE_PATTERN = /^\+?[0-9][0-9 \-]{6,18}$/;

@Component({
  selector: 'app-patients',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, TitleCasePipe],
  templateUrl: './patients.component.html',
})
export class PatientsComponent implements OnInit, OnDestroy {
  private readonly fb = inject(FormBuilder);
  private readonly patientService = inject(PatientService);

  readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.pattern(/\S/), Validators.maxLength(100)]],
    gender: ['', [Validators.required]],
    age: this.fb.control<number | null>(null, [Validators.required, Validators.min(1), Validators.max(120)]),
    phoneNumber: ['', [Validators.required, Validators.pattern(PHONE_PATTERN)]],
  });

  patients: Patient[] = [];
  searchText = '';
  loading = true;
  listError = '';

  submitted = false;
  saving = false;
  successMessage = '';
  formError = '';
  serverFieldErrors: Record<string, string> = {};

  private readonly load$ = new Subject<void>();
  private readonly typed$ = new Subject<void>();
  private readonly subscriptions = new Subscription();

  ngOnInit(): void {
    // Wait 300 ms after the last keystroke before calling the search endpoint.
    this.subscriptions.add(this.typed$.pipe(debounceTime(300)).subscribe(() => this.load$.next()));

    this.subscriptions.add(
      this.load$
        .pipe(
          tap(() => {
            this.loading = true;
            this.listError = '';
          }),
          switchMap(() =>
            this.patientService.search(this.searchText).pipe(
              catchError((err) => {
                this.listError = errorMessage(err);
                return of<Patient[]>([]);
              }),
            ),
          ),
        )
        .subscribe((list) => {
          this.patients = list;
          this.loading = false;
        }),
    );

    this.load$.next();
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }

  onSearchInput(value: string): void {
    this.searchText = value;
    this.typed$.next();
  }

  clearSearch(): void {
    this.searchText = '';
    this.load$.next();
  }

  /** First error message to show under a field, or '' when the field is fine. */
  fieldError(field: 'name' | 'gender' | 'age' | 'phoneNumber'): string {
    const control = this.form.controls[field];
    const show = control.touched || this.submitted;
    if (show && control.errors) {
      const e = control.errors;
      switch (field) {
        case 'name':
          return e['maxlength'] ? 'Name must be 100 characters or fewer.' : 'Enter the patient’s name.';
        case 'gender':
          return 'Select a gender.';
        case 'age':
          return e['required'] ? 'Enter the age.' : 'Age must be between 1 and 120.';
        case 'phoneNumber':
          return e['required'] ? 'Enter a phone number.' : 'Use 7–19 digits. Spaces, dashes and a leading + are fine.';
      }
    }
    return this.serverFieldErrors[field] ?? '';
  }

  register(): void {
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
    this.patientService
      .register({
        name: value.name.trim(),
        gender: value.gender as Gender,
        age: value.age as number,
        phoneNumber: value.phoneNumber.trim(),
      })
      .subscribe({
        next: (patient) => {
          this.saving = false;
          this.submitted = false;
          this.successMessage = `${patient.name} was registered.`;
          this.form.reset({ name: '', gender: '', age: null, phoneNumber: '' });
          this.load$.next();
        },
        error: (err) => {
          this.saving = false;
          this.formError = errorMessage(err);
          this.serverFieldErrors = fieldErrorsOf(err);
        },
      });
  }
}
