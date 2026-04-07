package com.example.patientservice.Service;

import com.example.patientservice.Exception.EmailAlreadyExistsException;
import com.example.patientservice.Exception.PatientNotFindException;
import com.example.patientservice.Model.Patient;
import com.example.patientservice.Repository.PatientRepository;
import com.example.patientservice.dto.PatientRequestDTO;
import com.example.patientservice.dto.PatientResponseDTO;
import com.example.patientservice.grpc.BillingServiceGrpcClient;
import com.example.patientservice.kafka.kafkaProducer;
import com.example.patientservice.mapper.PatientMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

//business logic for patient management and DTO conversion
@Service
public class PatientService {
    private PatientRepository patientRepository;
    private final com.example.patientservice.grpc.BillingServiceGrpcClient billingServiceGrpcClient;
    private final kafkaProducer kafkaProducer;

    public PatientService(PatientRepository patientRepository, BillingServiceGrpcClient billingServiceGrpcClient,
                          kafkaProducer kafkaProducer) {
        this.billingServiceGrpcClient = billingServiceGrpcClient;
        this.patientRepository = patientRepository;
        this.kafkaProducer = kafkaProducer;
    }

    public List<PatientResponseDTO> getPatients() {
        List<Patient> patients = patientRepository.findAll();
        return patients.stream().map(PatientMapper::toDTO).toList();
    }

    public PatientResponseDTO createPatient(PatientRequestDTO patientRequestDTO) {

        // email must be unique , if already exits throw exception
            if (patientRepository.existsByEmail(patientRequestDTO.getEmail())) {
                throw new EmailAlreadyExistsException("Email already exits: " + patientRequestDTO.getEmail());
            }
            Patient newpatient = patientRepository.save(
                PatientMapper.toModel(patientRequestDTO));

            kafkaProducer.sendEvent(newpatient);
            billingServiceGrpcClient.createBillingAccount(newpatient.getId().toString(), newpatient.getName(), newpatient.getEmail());
        return PatientMapper.toDTO(newpatient);
    }

    public PatientResponseDTO updatePatient(UUID id, PatientRequestDTO patientRequestDTO) {
        Patient existingPatient = patientRepository.findById(id).orElseThrow(() -> new PatientNotFindException("Patient not found with id: " + id));

        if (patientRepository.existsByEmailAndIdNot(patientRequestDTO.getEmail(), id)) {
            throw new EmailAlreadyExistsException("Email already exits: " + patientRequestDTO.getEmail());
        }

        existingPatient.setName(patientRequestDTO.getName());
        existingPatient.setEmail(patientRequestDTO.getEmail());
        existingPatient.setAddress(patientRequestDTO.getAddress());
        existingPatient.setDateOfBirth(LocalDate.parse(patientRequestDTO.getDateOfBirth()));
        Patient updatedPatient = patientRepository.save(existingPatient);
        return PatientMapper.toDTO(updatedPatient);
    }


    public Void deletePatient(UUID id) {
        Patient existingPatient = patientRepository.findById(id).orElseThrow(() -> new PatientNotFindException("Patient not found with id: " + id));

        patientRepository.deleteById(id);
        return null;
    }
}
