package com.example.opd.config;

import com.example.opd.entity.Appointment;
import com.example.opd.entity.Gender;
import com.example.opd.entity.Patient;
import com.example.opd.repository.AppointmentRepository;
import com.example.opd.repository.PatientRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * Optional sample data. Only runs when app.demo-data.enabled=true and the patients table is empty.
 * Rahul Patel is intentionally NOT seeded so you can register him live in the demo.
 */
@Component
@ConditionalOnProperty(name = "app.demo-data.enabled", havingValue = "true")
public class DemoDataLoader implements CommandLineRunner {

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final Clock clock;

    public DemoDataLoader(PatientRepository patientRepository, AppointmentRepository appointmentRepository, Clock clock) {
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.clock = clock;
    }

    @Override
    public void run(String... args) {
        if (patientRepository.count() > 0) {
            return;
        }
        Patient anita = patientRepository.save(new Patient("Anita Sharma", Gender.FEMALE, 34, "9876500001"));
        patientRepository.save(new Patient("Vikram Singh", Gender.MALE, 52, "9876500002"));
        patientRepository.save(new Patient("Meera Joshi", Gender.FEMALE, 27, "9876500003"));

        LocalDateTime now = LocalDateTime.now(clock).withSecond(0).withNano(0);
        appointmentRepository.save(new Appointment(anita, "Dr. Mehta", now));
    }
}
