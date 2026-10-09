import { HttpErrorResponse } from '@angular/common/http';
import { environment } from '../../environments/environment';

interface ApiErrorBody {
  message?: string;
  fieldErrors?: Record<string, string>;
}

/** Turns any HTTP failure into one readable sentence for the user. */
export function errorMessage(err: unknown): string {
  if (err instanceof HttpErrorResponse) {
    if (err.status === 0) {
      return `Cannot reach the server at ${environment.apiBaseUrl}. Check that the backend is running.`;
    }
    const body = err.error as ApiErrorBody | null;
    if (body && typeof body === 'object' && body.message) {
      return body.message;
    }
    return `The request failed (HTTP ${err.status}).`;
  }
  return 'Something went wrong. Please try again.';
}

/** Field-level messages sent by the backend validation (empty object if none). */
export function fieldErrorsOf(err: unknown): Record<string, string> {
  if (err instanceof HttpErrorResponse) {
    const body = err.error as ApiErrorBody | null;
    if (body && typeof body === 'object' && body.fieldErrors) {
      return body.fieldErrors;
    }
  }
  return {};
}
