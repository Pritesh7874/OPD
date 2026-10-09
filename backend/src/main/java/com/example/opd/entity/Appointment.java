package com.example.opd.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "appointments",
        indexes = @Index(name = "idx_appointment_date_time", columnList = "appointment_date_time"))
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many appointments belong to one patient.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(name = "doctor_name", nullable = false, length = 100)
    private String doctorName;

    // Stored as a plain DATETIME in the server's local time zone (no zone conversion).
    @Column(name = "appointment_date_time", nullable = false)
    private LocalDateTime appointmentDateTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AppointmentStatus status;

    protected Appointment() {
        // required by JPA
    }

    public Appointment(Patient patient, String doctorName, LocalDateTime appointmentDateTime) {
        this.patient = patient;
        this.doctorName = doctorName;
        this.appointmentDateTime = appointmentDateTime;
        this.status = AppointmentStatus.SCHEDULED;
    }

    public Long getId() { return id; }
    public Patient getPatient() { return patient; }
    public String getDoctorName() { return doctorName; }
    public LocalDateTime getAppointmentDateTime() { return appointmentDateTime; }
    public AppointmentStatus getStatus() { return status; }
    public void setStatus(AppointmentStatus status) { this.status = status; }
}
