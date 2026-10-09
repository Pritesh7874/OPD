package com.example.opd.dto;

import com.example.opd.entity.Gender;
import com.example.opd.entity.Patient;

public record PatientResponse(Long id, String name, Gender gender, Integer age, String phoneNumber) {

    public static PatientResponse from(Patient patient) {
        return new PatientResponse(patient.getId(), patient.getName(), patient.getGender(),
                patient.getAge(), patient.getPhoneNumber());
    }
}
