package com.example.opd.service;

import com.example.opd.dto.PatientRequest;
import com.example.opd.dto.PatientResponse;
import com.example.opd.entity.Patient;
import com.example.opd.exception.ResourceNotFoundException;
import com.example.opd.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PatientService {

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Transactional
    public PatientResponse register(PatientRequest request) {
        Patient patient = new Patient(
                request.name().trim(),
                request.gender(),
                request.age(),
                request.phoneNumber().trim());
        return PatientResponse.from(patientRepository.save(patient));
    }

    @Transactional(readOnly = true)
    public List<PatientResponse> findAll() {
        return patientRepository.findAllByOrderByNameAsc().stream().map(PatientResponse::from).toList();
    }

    /** Matches part of the name (any case) or part of the phone number. Blank query returns everyone. */
    @Transactional(readOnly = true)
    public List<PatientResponse> search(String query) {
        String text = query == null ? "" : query.trim();
        if (text.isEmpty()) {
            return findAll();
        }
        return patientRepository
                .findByNameContainingIgnoreCaseOrPhoneNumberContainingOrderByNameAsc(text, text)
                .stream().map(PatientResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public PatientResponse getById(Long id) {
        return PatientResponse.from(getEntity(id));
    }

    Patient getEntity(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id " + id));
    }
}
