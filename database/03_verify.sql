-- Use after the demo to prove the data is really stored in MySQL.
USE opd_management;

SHOW TABLES;

SELECT * FROM patients ORDER BY id DESC;

SELECT a.id, p.name AS patient, a.doctor_name, a.appointment_date_time, a.status
FROM appointments a JOIN patients p ON p.id = a.patient_id
ORDER BY a.id DESC;

SELECT c.id, p.name AS patient, a.doctor_name, c.blood_pressure, c.temperature,
       c.notes, c.consultation_date_time, c.status AS consultation_status, a.status AS appointment_status
FROM consultations c
JOIN appointments a ON a.id = c.appointment_id
JOIN patients p ON p.id = a.patient_id
ORDER BY c.id DESC;
