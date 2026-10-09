import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Consultation, ConsultationCreateRequest, ConsultationDetails } from '../models/consultation.model';

@Injectable({ providedIn: 'root' })
export class ConsultationService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiBaseUrl}/consultations`;

  /** Starts a consultation (and saves the first draft). */
  create(request: ConsultationCreateRequest): Observable<Consultation> {
    return this.http.post<Consultation>(this.url, request);
  }

  saveDraft(id: number, details: ConsultationDetails): Observable<Consultation> {
    return this.http.put<Consultation>(`${this.url}/${id}`, details);
  }

  /** Saves the given details and completes the consultation in one backend transaction. */
  complete(id: number, details: ConsultationDetails): Observable<Consultation> {
    return this.http.put<Consultation>(`${this.url}/${id}/complete`, details);
  }

  /** The server answers 204 (null here) when no consultation has been started yet. */
  getByAppointment(appointmentId: number): Observable<Consultation | null> {
    return this.http.get<Consultation | null>(`${this.url}/appointment/${appointmentId}`);
  }

  /** Completed consultations for one patient, newest first. */
  historyForPatient(patientId: number): Observable<Consultation[]> {
    return this.http.get<Consultation[]>(`${this.url}/patient/${patientId}`);
  }
}
