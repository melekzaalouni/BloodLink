package tn.bloodlink.donor.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tn.bloodlink.donor.dto.DonorRequest;
import tn.bloodlink.donor.dto.DonorResponse;
import tn.bloodlink.donor.service.DonorService;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/donors")
@Tag(name = "Donors")
public class DonorController {

    private final DonorService service;

    public DonorController(DonorService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Create a donor")
    @ApiResponse(responseCode = "201", description = "Donor created")
    @ApiResponse(responseCode = "400", description = "Validation failed")
    @ApiResponse(responseCode = "409", description = "Email already used")
    public ResponseEntity<DonorResponse> create(@Valid @RequestBody DonorRequest request) {
        DonorResponse created = service.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a donor by id")
    @ApiResponse(responseCode = "200", description = "Donor found")
    @ApiResponse(responseCode = "404", description = "Donor not found")
    public DonorResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @GetMapping
    @Operation(summary = "List donors (paginated)")
    @ApiResponse(responseCode = "200", description = "Page of donors")
    public Page<DonorResponse> list(@PageableDefault(size = 20) Pageable pageable) {
        return service.list(pageable);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a donor")
    @ApiResponse(responseCode = "200", description = "Donor updated")
    @ApiResponse(responseCode = "400", description = "Validation failed")
    @ApiResponse(responseCode = "404", description = "Donor not found")
    @ApiResponse(responseCode = "409", description = "Email already used")
    public DonorResponse update(@PathVariable Long id, @Valid @RequestBody DonorRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a donor")
    @ApiResponse(responseCode = "204", description = "Donor deleted")
    @ApiResponse(responseCode = "404", description = "Donor not found")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}