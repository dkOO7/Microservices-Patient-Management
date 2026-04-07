package com.example.patientservice.Controller;

import com.example.patientservice.Service.PatientService;
import com.example.patientservice.dto.PatientRequestDTO;
import com.example.patientservice.dto.PatientResponseDTO;
import com.example.patientservice.dto.validators.CreatePatientValidatorGroup;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.groups.Default;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/patients") // http://localhost:4000/patients
@Tag(name = "Patient", description = "API for managing patients")
public class PatientController {
    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping
    @Operation(summary = "Get all patients", description = "Retrieve a list of all patients")
    public ResponseEntity<List<PatientResponseDTO>> getPatients() {
        List<PatientResponseDTO> patients = patientService.getPatients();
        return ResponseEntity.ok(patients);
    }

    @PostMapping
    @Operation(summary = "Create a new patient", description = "Create a new patient with the provided information")
    public ResponseEntity<PatientResponseDTO> createPatient(@Validated({Default.class, CreatePatientValidatorGroup.class}) @RequestBody PatientRequestDTO patientDto) {
        PatientResponseDTO patientResponse = patientService.createPatient(patientDto);

        return ResponseEntity.ok().body(patientResponse);
    }

//    localhost:4000/patients/123e4567-e89b-12d3-a456-426614174000
    @PutMapping("/{id}")
    @Operation(summary = "Update an existing patient", description = "Update the information of an existing patient by ID")
    public ResponseEntity<PatientResponseDTO> updatePatient(@PathVariable UUID id , @Validated({Default.class}) @RequestBody PatientRequestDTO patientDto) {

        PatientResponseDTO patientResponse = patientService.updatePatient(id, patientDto);

        return ResponseEntity.ok().body(patientResponse);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a patient", description = "Delete an existing patient by ID")
    public ResponseEntity<Void> deletePatient(@PathVariable UUID id) {
        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }
}
