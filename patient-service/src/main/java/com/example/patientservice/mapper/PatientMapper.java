package com.example.patientservice.mapper;

import com.example.patientservice.Model.Patient;
import com.example.patientservice.dto.PatientRequestDTO;
import com.example.patientservice.dto.PatientResponseDTO;

import java.time.LocalDate;

public class PatientMapper {
    public static PatientResponseDTO toDTO(Patient p){
        PatientResponseDTO PatientDto = new PatientResponseDTO();
        PatientDto.setID(p.getId().toString());
        PatientDto.setName(p.getName());
        PatientDto.setAddress(p.getAddress());
        PatientDto.setEmail(p.getEmail());
        PatientDto.setDateOfBirth(p.getDateOfBirth().toString());
        return PatientDto;
    }

    public static Patient toModel(PatientRequestDTO patientDto ){

        System.out.println("DOB FROM REQUEST: " + patientDto.getDateOfBirth());

        Patient patient = new Patient();

        patient.setName(patientDto.getName());
        patient.setAddress(patientDto.getAddress());
        patient.setEmail(patientDto.getEmail());

        patient.setDateOfBirth(LocalDate.parse(patientDto.getDateOfBirth()));
        patient.setRegisteredDate(LocalDate.parse(patientDto.getRegistrationDate()));

        return patient;
    }
}
