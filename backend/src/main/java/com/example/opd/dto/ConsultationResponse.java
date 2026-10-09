package com.example.opd.dto;

import com.example.opd.entity.Appointment;
import com.example.opd.entity.Consultation;
import com.example.opd.entity.ConsultationStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ConsultationResponse(
        Long id,
        Long appointmentId,
        Long patientId,
        String patientName,
        String doctorName,
        LocalDateTime appointmentDateTime,
        String bloodPressure,
        BigDecimal temperature,
        String notes,
        LocalDateTime consultationDateTime,
        ConsultationStatus status
) {

    public static ConsultationResponse from(Consultation consultation) {
        Appointment appointment = consultation.getAppointment();
        return new ConsultationResponse(
                consultation.getId(),
                appointment.getId(),
                appointment.getPatient().getId(),
                appointment.getPatient().getName(),
                appointment.getDoctorName(),
                appointment.getAppointmentDateTime(),
                consultation.getBloodPressure(),
                consultation.getTemperature(),
                consultation.getNotes(),
                consultation.getConsultationDateTime(),
                consultation.getStatus());
    }
}
