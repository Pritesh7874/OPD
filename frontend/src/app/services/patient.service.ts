import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Patient, PatientRequest } from '../models/patient.model';

@Injectable({ providedIn: 'root' })
export class PatientService {
  private readonly http = inject(HttpClient);
  private readonly url = `${environment.apiBaseUrl}/patients`;

  register(request: PatientRequest): Observable<Patient> {
    return this.http.post<Patient>(this.url, request);
  }

  list(): Observable<Patient[]> {
    return this.http.get<Patient[]>(this.url);
  }

  /** Blank text lists everyone; otherwise matches name or phone number. */
  search(text: string): Observable<Patient[]> {
    const query = text.trim();
    if (!query) {
      return this.list();
    }
    return this.http.get<Patient[]>(`${this.url}/search`, { params: new HttpParams().set('query', query) });
  }

  get(id: number): Observable<Patient> {
    return this.http.get<Patient>(`${this.url}/${id}`);
  }
}
