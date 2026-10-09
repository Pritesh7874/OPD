package com.example.opd.dto;

import com.example.opd.entity.Gender;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PatientRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 100, message = "Name must be at most 100 characters")
        String name,

        @NotNull(message = "Gender is required")
        Gender gender,

        @NotNull(message = "Age is required")
        @Min(value = 1, message = "Age must be greater than zero")
        @Max(value = 120, message = "Age must be 120 or less")
        Integer age,

        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^\\+?[0-9][0-9 \\-]{6,18}$",
                message = "Phone number must be 7-19 digits (spaces, dashes and a leading + are allowed)")
        String phoneNumber
) {
}
