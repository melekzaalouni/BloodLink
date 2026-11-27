package tn.bloodlink.donor.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tn.bloodlink.donor.dto.AppointmentRequest;
import tn.bloodlink.donor.dto.AppointmentResponse;
import tn.bloodlink.donor.service.AppointmentService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/donors/{donorId}/appointments")
@Tag(name = "Appointments")
public class AppointmentController {

    private final AppointmentService service;

    public AppointmentController(AppointmentService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Book a donation appointment for a donor")
    @ApiResponse(responseCode = "201", description = "Appointment booked")
    @ApiResponse(responseCode = "400", description = "Validation failed")
    @ApiResponse(responseCode = "404", description = "Donor not found")
    @ApiResponse(responseCode = "422", description = "Less than 56 days since the last donation (R3)")
    public ResponseEntity<AppointmentResponse> book(@PathVariable Long donorId,
            @Valid @RequestBody AppointmentRequest request) {
        AppointmentResponse created = service.book(donorId, request);
        // Pas d'endpoint GET /{id} dans le contrat : Location pointe vers la collection
        // du donneur
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().build().toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping
    @Operation(summary = "List a donor's appointments")
    @ApiResponse(responseCode = "200", description = "List of appointments")
    @ApiResponse(responseCode = "404", description = "Donor not found")
    public List<AppointmentResponse> list(@PathVariable Long donorId) {
        return service.list(donorId);
    }
}