package com.example.opd.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "consultations")
public class Consultation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // unique = true: the database itself guarantees "at most one consultation per appointment".
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "appointment_id", nullable = false, unique = true)
    private Appointment appointment;

    @Column(name = "blood_pressure", length = 20)
    private String bloodPressure;

    @Column(precision = 4, scale = 1)
    private BigDecimal temperature;

    @Column(length = 1000)
    private String notes;

    // Set once, by the server, when the consultation record is created.
    @Column(name = "consultation_date_time", nullable = false)
    private LocalDateTime consultationDateTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ConsultationStatus status;

    protected Consultation() {
        // required by JPA
    }

    public Consultation(Appointment appointment, LocalDateTime consultationDateTime) {
        this.appointment = appointment;
        this.consultationDateTime = consultationDateTime;
        this.status = ConsultationStatus.IN_PROGRESS;
    }

    /** Copies vitals and notes onto this record. Blank strings are stored as null. */
    public void applyDetails(String bloodPressure, BigDecimal temperature, String notes) {
        this.bloodPressure = blankToNull(bloodPressure);
        this.temperature = temperature;
        this.notes = blankToNull(notes);
    }

    /** How many vital fields (blood pressure, temperature) have a value. */
    public int countProvidedVitals() {
        int count = 0;
        if (bloodPressure != null) count++;
        if (temperature != null) count++;
        return count;
    }

    private static String blankToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public Long getId() { return id; }
    public Appointment getAppointment() { return appointment; }
    public String getBloodPressure() { return bloodPressure; }
    public BigDecimal getTemperature() { return temperature; }
    public String getNotes() { return notes; }
    public LocalDateTime getConsultationDateTime() { return consultationDateTime; }
    public ConsultationStatus getStatus() { return status; }
    public void setStatus(ConsultationStatus status) { this.status = status; }
}
