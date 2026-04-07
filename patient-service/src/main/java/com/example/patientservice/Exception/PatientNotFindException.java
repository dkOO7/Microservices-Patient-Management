package com.example.patientservice.Exception;

public class PatientNotFindException extends RuntimeException {
    public PatientNotFindException(String message) {
        super(message);
    }
}
