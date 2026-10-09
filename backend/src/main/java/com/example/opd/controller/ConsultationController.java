package com.example.opd.controller;

import com.example.opd.dto.ConsultationCreateRequest;
import com.example.opd.dto.ConsultationResponse;
import com.example.opd.dto.ConsultationUpdateRequest;
import com.example.opd.service.ConsultationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/consultations")
public class ConsultationController {

    private final ConsultationService consultationService;

    public ConsultationController(ConsultationService consultationService) {
        this.consultationService = consultationService;
    }

    @PostMapping
    public ResponseEntity<ConsultationResponse> create(@Valid @RequestBody ConsultationCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(consultationService.create(request));
    }

    @PutMapping("/{id}")
    public ConsultationResponse saveDraft(@PathVariable Long id,
                                          @Valid @RequestBody ConsultationUpdateRequest request) {
        return consultationService.saveDraft(id, request);
    }

    @PutMapping("/{id}/complete")
    public ConsultationResponse complete(@PathVariable Long id,
                                         @Valid @RequestBody(required = false) ConsultationUpdateRequest request) {
        return consultationService.complete(id, request);
    }

    /** 200 with the consultation, or 204 No Content when none has been started for this appointment. */
    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<ConsultationResponse> getByAppointment(@PathVariable Long appointmentId) {
        return consultationService.findByAppointment(appointmentId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/patient/{patientId}")
    public List<ConsultationResponse> historyForPatient(@PathVariable Long patientId) {
        return consultationService.findCompletedForPatient(patientId);
    }
}
