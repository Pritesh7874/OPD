package com.example.opd.repository;

import com.example.opd.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // "join fetch" loads the patient in the same query (no extra query per row).
    @Query("select a from Appointment a join fetch a.patient order by a.appointmentDateTime desc")
    List<Appointment> findAllDetailed();

    @Query("select a from Appointment a join fetch a.patient "
            + "where a.appointmentDateTime >= :start and a.appointmentDateTime < :end "
            + "order by a.appointmentDateTime asc")
    List<Appointment> findBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("select a from Appointment a join fetch a.patient where a.id = :id")
    Optional<Appointment> findDetailedById(@Param("id") Long id);
}
