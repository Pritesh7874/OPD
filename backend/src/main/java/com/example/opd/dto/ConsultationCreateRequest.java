package com.example.opd.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ConsultationCreateRequest(
        @NotNull(message = "Appointment is required")
        Long appointmentId,

        @Pattern(regexp = "^\\d{2,3}/\\d{2,3}$", message = "Blood pressure must look like 120/80")
        String bloodPressure,

        @DecimalMin(value = "30.0", message = "Temperature must be between 30 and 110")
        @DecimalMax(value = "110.0", message = "Temperature must be between 30 and 110")
        @Digits(integer = 3, fraction = 1, message = "Temperature can have at most one decimal place")
        BigDecimal temperature,

        @Size(max = 1000, message = "Notes must be at most 1000 characters")
        String notes
) {
}
