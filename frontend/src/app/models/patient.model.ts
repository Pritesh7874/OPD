export type Gender = 'MALE' | 'FEMALE' | 'OTHER';

export interface Patient {
  id: number;
  name: string;
  gender: Gender;
  age: number;
  phoneNumber: string;
}

export interface PatientRequest {
  name: string;
  gender: Gender;
  age: number;
  phoneNumber: string;
}
