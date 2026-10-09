package com.example.opd.repository;

import com.example.opd.entity.Consultation;
import com.example.opd.entity.ConsultationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConsultationRepository extends JpaRepository<Consultation, Long> {

    boolean existsByAppointmentId(Long appointmentId);

    @Query("select c from Consultation c join fetch c.appointment a join fetch a.patient where c.id = :id")
    Optional<Consultation> findDetailedById(@Param("id") Long id);

    @Query("select c from Consultation c join fetch c.appointment a join fetch a.patient where a.id = :appointmentId")
    Optional<Consultation> findDetailedByAppointmentId(@Param("appointmentId") Long appointmentId);

    @Query("select c from Consultation c join fetch c.appointment a join fetch a.patient p "
            + "where p.id = :patientId and c.status = :status "
            + "order by c.consultationDateTime desc")
    List<Consultation> findByPatientIdAndStatus(@Param("patientId") Long patientId,
                                                @Param("status") ConsultationStatus status);
}
