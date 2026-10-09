import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Appointment, AppointmentRequest } from '../models/appointment.model';

@Injectable({ providedIn: 'root' })
export class AppointmentService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiBaseUrl}/appointments`;

  book(request: AppointmentRequest): Observable<Appointment> {
    return this.http.post<Appointment>(this.url, request);
  }

  today(): Observable<Appointment[]> {
    return this.http.get<Appointment[]>(`${this.url}/today`);
  }

  list(): Observable<Appointment[]> {
    return this.http.get<Appointment[]>(this.url);
  }

  get(id: number): Observable<Appointment> {
    return this.http.get<Appointment>(`${this.url}/${id}`);
  }
}
