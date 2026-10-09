import { Routes } from '@angular/router';
import { AppointmentsComponent } from './appointments/appointments.component';
import { ConsultationComponent } from './consultation/consultation.component';
import { PatientsComponent } from './patients/patients.component';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'patients' },
  { path: 'patients', component: PatientsComponent, title: 'Patients · OPD Desk' },
  { path: 'appointments', component: AppointmentsComponent, title: 'Appointments · OPD Desk' },
  { path: 'consultation', component: ConsultationComponent, title: 'Consultations · OPD Desk' },
  { path: 'consultation/:appointmentId', component: ConsultationComponent, title: 'Consultation · OPD Desk' },
  { path: '**', redirectTo: 'patients' },
];
