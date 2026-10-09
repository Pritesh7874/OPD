export type ConsultationStatus = 'IN_PROGRESS' | 'COMPLETED';

export interface Consultation {
  id: number;
  appointmentId: number;
  patientId: number;
  patientName: string;
  doctorName: string;
  appointmentDateTime: string;
  bloodPressure: string | null;
  temperature: number | null;
  notes: string | null;
  consultationDateTime: string;
  status: ConsultationStatus;
}

/** Vitals and notes the doctor types in. Empty values are sent as null. */
export interface ConsultationDetails {
  bloodPressure: string | null;
  temperature: number | null;
  notes: string | null;
}

export interface ConsultationCreateRequest extends ConsultationDetails {
  appointmentId: number;
}
