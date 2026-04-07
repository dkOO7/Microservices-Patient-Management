package com.example.patientservice.dto;


// store everything in string format , easy to convert to json and send to other services
public class PatientResponseDTO {
    private String ID;

    private String Name;
    private String email;
    private String Address;
    private String DateOfBirth;


    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public String getDateOfBirth() {
        return DateOfBirth;
    }

    public void setDateOfBirth(String dateofBirth) {
        DateOfBirth = dateofBirth;
    }


}
