package com.example.opd.service;

import com.example.opd.dto.ConsultationCreateRequest;
import com.example.opd.dto.ConsultationResponse;
import com.example.opd.dto.ConsultationUpdateRequest;
import com.example.opd.entity.Appointment;
import com.example.opd.entity.AppointmentStatus;
import com.example.opd.entity.Consultation;
import com.example.opd.entity.ConsultationStatus;
import com.example.opd.exception.BusinessRuleException;
import com.example.opd.exception.ConflictException;
import com.example.opd.exception.ResourceNotFoundException;
import com.example.opd.repository.AppointmentRepository;
import com.example.opd.repository.ConsultationRepository;
import com.example.opd.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ConsultationService {

    /** A consultation can only be completed when at least this many vitals are filled in. */
    static final int MIN_VITALS_FOR_COMPLETION = 2;

    private final ConsultationRepository consultationRepository;
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final Clock clock;

    public ConsultationService(ConsultationRepository consultationRepository,
                               AppointmentRepository appointmentRepository,
                               PatientRepository patientRepository,
                               Clock clock) {
        this.consultationRepository = consultationRepository;
        this.appointmentRepository = appointmentRepository;
        this.patientRepository = patientRepository;
        this.clock = clock;
    }

    /** Starts a consultation (IN_PROGRESS) for an appointment and saves the first draft. */
    @Transactional
    public ConsultationResponse create(ConsultationCreateRequest request) {
        Appointment appointment = appointmentRepository.findDetailedById(request.appointmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Appointment not found with id " + request.appointmentId()));

        if (consultationRepository.existsByAppointmentId(appointment.getId())) {
            throw new ConflictException("A consultation already exists for appointment " + appointment.getId());
        }

        Consultation consultation = new Consultation(appointment, LocalDateTime.now(clock));
        consultation.applyDetails(request.bloodPressure(), request.temperature(), request.notes());

        if (appointment.getStatus() == AppointmentStatus.SCHEDULED) {
            appointment.setStatus(AppointmentStatus.IN_PROGRESS);
        }
        return ConsultationResponse.from(consultationRepository.save(consultation));
    }

    /** Saves a draft. Completed consultations are read-only. */
    @Transactional
    public ConsultationResponse saveDraft(Long id, ConsultationUpdateRequest request) {
        Consultation consultation = getDetailedEntity(id);
        requireInProgress(consultation);
        consultation.applyDetails(request.bloodPressure(), request.temperature(), request.notes());
        return ConsultationResponse.from(consultation);
    }

    /**
     * Completes a consultation. One transaction updates the consultation AND its appointment,
     * so they can never disagree. If the rule fails, nothing is saved.
     */
    @Transactional
    public ConsultationResponse complete(Long id, ConsultationUpdateRequest request) {
        Consultation consultation = getDetailedEntity(id);
        requireInProgress(consultation);

        if (request != null) {
            consultation.applyDetails(request.bloodPressure(), request.temperature(), request.notes());
        }
        if (consultation.countProvidedVitals() < MIN_VITALS_FOR_COMPLETION) {
            throw new BusinessRuleException("Enter at least " + MIN_VITALS_FOR_COMPLETION
                    + " vitals (blood pressure and temperature) before completing the consultation.");
        }

        consultation.setStatus(ConsultationStatus.COMPLETED);
        consultation.getAppointment().setStatus(AppointmentStatus.COMPLETED);
        return ConsultationResponse.from(consultation);
    }

    /** Empty result means "no consultation started yet for this appointment". */
    @Transactional(readOnly = true)
    public Optional<ConsultationResponse> findByAppointment(Long appointmentId) {
        if (!appointmentRepository.existsById(appointmentId)) {
            throw new ResourceNotFoundException("Appointment not found with id " + appointmentId);
        }
        return consultationRepository.findDetailedByAppointmentId(appointmentId).map(ConsultationResponse::from);
    }

    /** Completed consultations for one patient, newest first. */
    @Transactional(readOnly = true)
    public List<ConsultationResponse> findCompletedForPatient(Long patientId) {
        if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException("Patient not found with id " + patientId);
        }
        return consultationRepository.findByPatientIdAndStatus(patientId, ConsultationStatus.COMPLETED)
                .stream().map(ConsultationResponse::from).toList();
    }

    private Consultation getDetailedEntity(Long id) {
        return consultationRepository.findDetailedById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consultation not found with id " + id));
    }

    private void requireInProgress(Consultation consultation) {
        if (consultation.getStatus() == ConsultationStatus.COMPLETED) {
            throw new ConflictException("Consultation " + consultation.getId() + " is already completed.");
        }
    }
}
