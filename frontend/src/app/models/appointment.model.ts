export type AppointmentStatus = 'SCHEDULED' | 'IN_PROGRESS' | 'COMPLETED';

export interface Appointment {
  id: number;
  patientId: number;
  patientName: string;
  patientPhone: string;
  doctorName: string;
  /** ISO local date-time from the server, e.g. 2026-10-09T14:30:00 (no time zone). */
  appointmentDateTime: string;
  status: AppointmentStatus;
}

export interface AppointmentRequest {
  patientId: number;
  doctorName: string;
  /** Value of a datetime-local input, e.g. 2026-10-09T14:30 */
  appointmentDateTime: string;
}
