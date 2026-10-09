package com.example.opd;

import com.example.opd.repository.AppointmentRepository;
import com.example.opd.repository.ConsultationRepository;
import com.example.opd.repository.PatientRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end API tests: HTTP -> controller -> service -> JPA -> in-memory H2 database.
 * The "test" profile swaps MySQL for H2, so your real data is never touched.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OpdApiIntegrationTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private PatientRepository patientRepository;
    @Autowired private AppointmentRepository appointmentRepository;
    @Autowired private ConsultationRepository consultationRepository;

    @BeforeEach
    void cleanDatabase() {
        consultationRepository.deleteAll();
        appointmentRepository.deleteAll();
        patientRepository.deleteAll();
    }

    // ---------- helpers ----------

    private long createPatient(String name, String phone) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("gender", "MALE");
        body.put("age", 30);
        body.put("phoneNumber", phone);
        MvcResult result = mvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private long createAppointment(long patientId, LocalDateTime when) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("patientId", patientId);
        body.put("doctorName", "Dr. Mehta");
        body.put("appointmentDateTime", when.withNano(0).toString());
        MvcResult result = mvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private long startConsultation(long appointmentId) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("appointmentId", appointmentId);
        MvcResult result = mvc.perform(post("/api/consultations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private String vitals(String bloodPressure, Double temperature, String notes) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("bloodPressure", bloodPressure);
        body.put("temperature", temperature);
        body.put("notes", notes);
        return objectMapper.writeValueAsString(body);
    }

    // ---------- patients ----------

    @Test
    void registerPatient_returns201AndAppearsInList() throws Exception {
        createPatient("Rahul Patel", "9876543210");

        mvc.perform(get("/api/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Rahul Patel"))
                .andExpect(jsonPath("$[0].gender").value("MALE"));
    }

    @Test
    void registerPatient_withInvalidData_returns400WithFieldErrors() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("name", "");
        body.put("gender", "MALE");
        body.put("age", 0);
        body.put("phoneNumber", "");

        mvc.perform(post("/api/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.name").exists())
                .andExpect(jsonPath("$.fieldErrors.age").exists())
                .andExpect(jsonPath("$.fieldErrors.phoneNumber").exists());
    }

    @Test
    void searchPatients_findsByNameIgnoringCase_andByPhone() throws Exception {
        createPatient("Rahul Patel", "9876543210");
        createPatient("Anita Sharma", "9123456780");

        mvc.perform(get("/api/patients/search").param("query", "rahul"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Rahul Patel"));

        mvc.perform(get("/api/patients/search").param("query", "91234"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Anita Sharma"));

        mvc.perform(get("/api/patients/search").param("query", "nobody"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getPatient_unknownId_returns404() throws Exception {
        mvc.perform(get("/api/patients/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Patient not found with id 9999"));
    }

    // ---------- appointments ----------

    @Test
    void bookAppointment_forExistingPatient_appearsInToday_andNotYesterday() throws Exception {
        long patientId = createPatient("Rahul Patel", "9876543210");
        long todayId = createAppointment(patientId, LocalDateTime.now());
        createAppointment(patientId, LocalDateTime.now().minusDays(1));

        mvc.perform(get("/api/appointments/today"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(todayId))
                .andExpect(jsonPath("$[0].patientName").value("Rahul Patel"))
                .andExpect(jsonPath("$[0].status").value("SCHEDULED"));

        mvc.perform(get("/api/appointments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void bookAppointment_unknownPatient_returns404() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("patientId", 12345);
        body.put("doctorName", "Dr. Mehta");
        body.put("appointmentDateTime", LocalDateTime.now().withNano(0).toString());

        mvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNotFound());
    }

    @Test
    void bookAppointment_missingDoctorAndDate_returns400() throws Exception {
        long patientId = createPatient("Rahul Patel", "9876543210");
        Map<String, Object> body = new HashMap<>();
        body.put("patientId", patientId);
        body.put("doctorName", " ");

        mvc.perform(post("/api/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.doctorName").exists())
                .andExpect(jsonPath("$.fieldErrors.appointmentDateTime").exists());
    }

    @Test
    void getAppointment_unknownId_returns404() throws Exception {
        mvc.perform(get("/api/appointments/9999")).andExpect(status().isNotFound());
    }

    // ---------- consultations ----------

    @Test
    void fullConsultationFlow_completesAppointment_andShowsInPatientHistory() throws Exception {
        long patientId = createPatient("Rahul Patel", "9876543210");
        long appointmentId = createAppointment(patientId, LocalDateTime.now());

        // no consultation yet -> 204
        mvc.perform(get("/api/consultations/appointment/" + appointmentId))
                .andExpect(status().isNoContent());

        long consultationId = startConsultation(appointmentId);

        // starting a consultation moves the appointment to IN_PROGRESS
        mvc.perform(get("/api/appointments/" + appointmentId))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        // not shown in history until completed
        mvc.perform(get("/api/consultations/patient/" + patientId))
                .andExpect(jsonPath("$", hasSize(0)));

        mvc.perform(put("/api/consultations/" + consultationId + "/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(vitals("120/80", 98.6, "Mild fever, advised rest")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.bloodPressure").value("120/80"))
                .andExpect(jsonPath("$.temperature").value(98.6));

        mvc.perform(get("/api/appointments/" + appointmentId))
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        mvc.perform(get("/api/consultations/patient/" + patientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].doctorName").value("Dr. Mehta"))
                .andExpect(jsonPath("$[0].notes").value("Mild fever, advised rest"))
                .andExpect(jsonPath("$[0].consultationDateTime").exists());
    }

    @Test
    void completeConsultation_withOnlyOneVital_returns400_andNothingIsCompleted() throws Exception {
        long patientId = createPatient("Rahul Patel", "9876543210");
        long appointmentId = createAppointment(patientId, LocalDateTime.now());
        long consultationId = startConsultation(appointmentId);

        mvc.perform(put("/api/consultations/" + consultationId + "/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(vitals("120/80", null, "Only BP taken")))
                .andExpect(status().isBadRequest());

        mvc.perform(get("/api/appointments/" + appointmentId))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
        mvc.perform(get("/api/consultations/patient/" + patientId))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void completeConsultation_twice_returns409() throws Exception {
        long patientId = createPatient("Rahul Patel", "9876543210");
        long appointmentId = createAppointment(patientId, LocalDateTime.now());
        long consultationId = startConsultation(appointmentId);
        String body = vitals("120/80", 98.6, "Fine");

        mvc.perform(put("/api/consultations/" + consultationId + "/complete")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk());
        mvc.perform(put("/api/consultations/" + consultationId + "/complete")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void startConsultation_twiceForSameAppointment_returns409() throws Exception {
        long patientId = createPatient("Rahul Patel", "9876543210");
        long appointmentId = createAppointment(patientId, LocalDateTime.now());
        startConsultation(appointmentId);

        Map<String, Object> body = new HashMap<>();
        body.put("appointmentId", appointmentId);
        mvc.perform(post("/api/consultations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isConflict());
    }

    @Test
    void saveDraft_thenComplete_usesSavedValues() throws Exception {
        long patientId = createPatient("Rahul Patel", "9876543210");
        long appointmentId = createAppointment(patientId, LocalDateTime.now());
        long consultationId = startConsultation(appointmentId);

        mvc.perform(put("/api/consultations/" + consultationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(vitals("130/85", 99.1, "Draft notes")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        // complete with no body: the saved draft already has two vitals
        mvc.perform(put("/api/consultations/" + consultationId + "/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.bloodPressure").value("130/85"));
    }

    @Test
    void consultation_missingAppointmentOrPatient_returns404() throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("appointmentId", 9999);
        mvc.perform(post("/api/consultations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNotFound());

        mvc.perform(put("/api/consultations/9999/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(vitals("120/80", 98.6, "x")))
                .andExpect(status().isNotFound());

        mvc.perform(get("/api/consultations/patient/9999")).andExpect(status().isNotFound());
        mvc.perform(get("/api/consultations/appointment/9999")).andExpect(status().isNotFound());
    }
}
