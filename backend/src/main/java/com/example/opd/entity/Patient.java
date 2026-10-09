package com.example.opd.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "patients")
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Gender gender;

    @Column(nullable = false)
    private Integer age;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    protected Patient() {
        // required by JPA
    }

    public Patient(String name, Gender gender, Integer age, String phoneNumber) {
        this.name = name;
        this.gender = gender;
        this.age = age;
        this.phoneNumber = phoneNumber;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public Gender getGender() { return gender; }
    public Integer getAge() { return age; }
    public String getPhoneNumber() { return phoneNumber; }
}
