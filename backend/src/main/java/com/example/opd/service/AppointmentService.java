package com.example.opd.service;

import com.example.opd.dto.AppointmentRequest;
import com.example.opd.dto.AppointmentResponse;
import com.example.opd.entity.Appointment;
import com.example.opd.entity.Patient;
import com.example.opd.exception.ResourceNotFoundException;
import com.example.opd.repository.AppointmentRepository;
import com.example.opd.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final Clock clock;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              PatientRepository patientRepository,
                              Clock clock) {
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.clock = clock;
    }

    @Transactional
    public AppointmentResponse book(AppointmentRequest request) {
        Patient patient = patientRepository.findById(request.patientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id " + request.patientId()));
        Appointment appointment = new Appointment(patient, request.doctorName().trim(), request.appointmentDateTime());
        return AppointmentResponse.from(appointmentRepository.save(appointment));
    }

    /** Appointments from 00:00 (inclusive) to the next 00:00 (exclusive), by the server's local date. */
    @Transactional(readOnly = true)
    public List<AppointmentResponse> findToday() {
        LocalDate today = LocalDate.now(clock);
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();
        return appointmentRepository.findBetween(start, end).stream().map(AppointmentResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> findAll() {
        return appointmentRepository.findAllDetailed().stream().map(AppointmentResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public AppointmentResponse getById(Long id) {
        Appointment appointment = appointmentRepository.findDetailedById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id " + id));
        return AppointmentResponse.from(appointment);
    }
}
