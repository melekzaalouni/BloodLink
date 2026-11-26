package tn.bloodlink.donor.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DonorControllerIT {

    @Autowired
    MockMvc mvc;

    private static String donorJson(String email, String bloodType, LocalDate lastDonation) {
        String last = lastDonation == null ? "null" : "\"" + lastDonation + "\"";
        return """
                {"fullName":"Awa Diallo","email":"%s","phone":"22912345678","bloodType":"%s","lastDonation":%s}
                """.formatted(email, bloodType, last);
    }

    private static String uniqueEmail() {
        return UUID.randomUUID() + "@mail.com";
    }

    /** Crée un donneur et renvoie son id. */
    private long createDonor(String email, LocalDate lastDonation) throws Exception {
        MvcResult r = mvc.perform(post("/api/v1/donors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(donorJson(email, "O+", lastDonation)))
                .andExpect(status().isCreated())
                .andReturn();
        String body = r.getResponse().getContentAsString();
        return Long.parseLong(body.replaceAll(".*\"id\":(\\d+).*", "$1"));
    }

    @Test
    void creating_a_donor_returns_201_with_location() throws Exception {
        String email = uniqueEmail();
        mvc.perform(post("/api/v1/donors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(donorJson(email, "A-", null)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/v1/donors/")))
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    void invalid_blood_type_returns_400_with_details() throws Exception {
        mvc.perform(post("/api/v1/donors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(donorJson(uniqueEmail(), "Z+", null)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details", not(empty())));
    }

    @Test
    void malformed_json_returns_400() throws Exception {
        mvc.perform(post("/api/v1/donors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Malformed JSON request"));
    }

    @Test
    void unknown_donor_returns_404() throws Exception {
        mvc.perform(get("/api/v1/donors/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void unknown_route_returns_404_not_500() throws Exception {
        mvc.perform(get("/api/v1/unknown")).andExpect(status().isNotFound());
    }

    @Test
    void wrong_http_method_returns_405() throws Exception {
        mvc.perform(patch("/api/v1/donors/1")).andExpect(status().isMethodNotAllowed());
    }

    @Test
    void same_email_twice_returns_409() throws Exception {
        String email = uniqueEmail();
        createDonor(email, null);
        mvc.perform(post("/api/v1/donors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(donorJson(email, "O+", null)))
                .andExpect(status().isConflict());
    }

    @Test
    void put_returns_200_and_updates_the_donor() throws Exception {
        long id = createDonor(uniqueEmail(), null);
        String newEmail = uniqueEmail();
        mvc.perform(put("/api/v1/donors/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(donorJson(newEmail, "B+", null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(newEmail))
                .andExpect(jsonPath("$.bloodType").value("B+"));
    }

    @Test
    void delete_returns_204_then_404() throws Exception {
        long id = createDonor(uniqueEmail(), null);
        mvc.perform(delete("/api/v1/donors/" + id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/v1/donors/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void list_returns_200_with_a_page() throws Exception {
        createDonor(uniqueEmail(), null);
        mvc.perform(get("/api/v1/donors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", not(empty())));
    }

    // ---- Scénario 5 : rendez-vous et règle R3

    @Test
    void appointment_too_close_to_last_donation_returns_422() throws Exception {
        long id = createDonor(uniqueEmail(), LocalDate.now().minusDays(10));
        mvc.perform(post("/api/v1/donors/" + id + "/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"bankName":"CNTS Niamey","scheduledAt":"%s"}
                                """.formatted(LocalDateTime.now().plusDays(5).withNano(0))))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void appointment_after_56_days_returns_201_then_list_returns_200() throws Exception {
        long id = createDonor(uniqueEmail(), LocalDate.now().minusDays(10));
        mvc.perform(post("/api/v1/donors/" + id + "/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"bankName":"CNTS Niamey","scheduledAt":"%s"}
                                """.formatted(LocalDateTime.now().plusDays(60).withNano(0))))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.status").value("BOOKED"));

        mvc.perform(get("/api/v1/donors/" + id + "/appointments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void appointments_of_unknown_donor_return_404() throws Exception {
        mvc.perform(get("/api/v1/donors/9999/appointments")).andExpect(status().isNotFound());
    }

    @Test
    void appointment_in_the_past_returns_400() throws Exception {
        long id = createDonor(uniqueEmail(), null);
        mvc.perform(post("/api/v1/donors/" + id + "/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"bankName":"CNTS Niamey","scheduledAt":"2020-01-01T10:00:00"}
                                """))
                .andExpect(status().isBadRequest());
    }
}