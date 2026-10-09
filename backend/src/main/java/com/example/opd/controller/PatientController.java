package com.example.opd.controller;

import com.example.opd.dto.PatientRequest;
import com.example.opd.dto.PatientResponse;
import com.example.opd.service.PatientService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @PostMapping
    public ResponseEntity<PatientResponse> register(@Valid @RequestBody PatientRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(patientService.register(request));
    }

    @GetMapping
    public List<PatientResponse> list() {
        return patientService.findAll();
    }

    @GetMapping("/search")
    public List<PatientResponse> search(@RequestParam(name = "query", defaultValue = "") String query) {
        return patientService.search(query);
    }

    @GetMapping("/{id}")
    public PatientResponse get(@PathVariable Long id) {
        return patientService.getById(id);
    }
}
