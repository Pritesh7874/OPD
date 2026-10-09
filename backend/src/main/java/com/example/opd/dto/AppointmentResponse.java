package com.example.opd.dto;

import com.example.opd.entity.Appointment;
import com.example.opd.entity.AppointmentStatus;

import java.time.LocalDateTime;

public record AppointmentResponse(
        Long id,
        Long patientId,
        String patientName,
        String patientPhone,
        String doctorName,
        LocalDateTime appointmentDateTime,
        AppointmentStatus status
) {

    public static AppointmentResponse from(Appointment appointment) {
        return new AppointmentResponse(
                appointment.getId(),
                appointment.getPatient().getId(),
                appointment.getPatient().getName(),
                appointment.getPatient().getPhoneNumber(),
                appointment.getDoctorName(),
                appointment.getAppointmentDateTime(),
                appointment.getStatus());
    }
}
