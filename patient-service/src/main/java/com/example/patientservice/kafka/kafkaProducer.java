package com.example.patientservice.kafka;

import com.example.patientservice.Model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import patient.events.PatientEvent;

@Service
public class kafkaProducer {
    private static final Logger log = LoggerFactory.getLogger(kafkaProducer.class);
    private final KafkaTemplate<String, byte[]> kafkaTemplate;

    kafkaProducer (KafkaTemplate<String, byte[]> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendEvent(Patient patient ){
        PatientEvent event = PatientEvent.newBuilder()
                .setId(patient.getId().toString())
                .setName(patient.getName())
                .setEmail(patient.getEmail())
                .setEventType("PatientCreated")
                .build();

        try{
            kafkaTemplate.send("patient",event.toByteArray());
        }catch ( Exception e ){
            log.error("Error sending Patient created event in kafka: {}", e.getMessage() );
        }
    }
}
