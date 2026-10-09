package com.example.opd.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record AppointmentRequest(
        @NotNull(message = "Patient is required")
        Long patientId,

        @NotBlank(message = "Doctor name is required")
        @Size(max = 100, message = "Doctor name must be at most 100 characters")
        String doctorName,

        @NotNull(message = "Appointment date and time is required")
        LocalDateTime appointmentDateTime
) {
}
