-- OPTIONAL. By default Hibernate creates these tables itself (spring.jpa.hibernate.ddl-auto=update).
-- Run this file only if you want to see or create the schema manually BEFORE starting the backend.
-- It matches the JPA entities exactly.
USE opd_management;

CREATE TABLE IF NOT EXISTS patients (
  id            BIGINT       NOT NULL AUTO_INCREMENT,
  name          VARCHAR(100) NOT NULL,
  gender        VARCHAR(10)  NOT NULL,
  age           INT          NOT NULL,
  phone_number  VARCHAR(20)  NOT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS appointments (
  id                     BIGINT       NOT NULL AUTO_INCREMENT,
  patient_id             BIGINT       NOT NULL,
  doctor_name            VARCHAR(100) NOT NULL,
  appointment_date_time  DATETIME(6)  NOT NULL,
  status                 VARCHAR(20)  NOT NULL,
  PRIMARY KEY (id),
  KEY idx_appointment_date_time (appointment_date_time),
  CONSTRAINT fk_appointment_patient FOREIGN KEY (patient_id) REFERENCES patients (id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS consultations (
  id                      BIGINT        NOT NULL AUTO_INCREMENT,
  appointment_id          BIGINT        NOT NULL,
  blood_pressure          VARCHAR(20)   NULL,
  temperature             DECIMAL(4,1)  NULL,
  notes                   VARCHAR(1000) NULL,
  consultation_date_time  DATETIME(6)   NOT NULL,
  status                  VARCHAR(20)   NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_consultation_appointment (appointment_id),  -- at most ONE consultation per appointment
  CONSTRAINT fk_consultation_appointment FOREIGN KEY (appointment_id) REFERENCES appointments (id)
) ENGINE=InnoDB;
