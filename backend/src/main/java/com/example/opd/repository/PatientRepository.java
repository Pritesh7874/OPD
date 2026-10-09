package com.example.opd.repository;

import com.example.opd.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    List<Patient> findAllByOrderByNameAsc();

    List<Patient> findByNameContainingIgnoreCaseOrPhoneNumberContainingOrderByNameAsc(String name, String phoneNumber);
}
