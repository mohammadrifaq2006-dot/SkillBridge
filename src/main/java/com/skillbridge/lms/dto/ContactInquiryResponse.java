package com.skillbridge.lms.dto;

import com.skillbridge.lms.entity.ContactInquiry;

import java.time.LocalDateTime;

public class ContactInquiryResponse {

    private Long id;
    private String name;
    private String email;
    private String subject;
    private String message;
    private LocalDateTime creationDate;

    public ContactInquiryResponse() {
    }

    public static ContactInquiryResponse fromEntity(ContactInquiry inquiry) {
        ContactInquiryResponse dto = new ContactInquiryResponse();
        dto.setId(inquiry.getId());
        dto.setName(inquiry.getName());
        dto.setEmail(inquiry.getEmail());
        dto.setSubject(inquiry.getSubject());
        dto.setMessage(inquiry.getMessage());
        dto.setCreationDate(inquiry.getCreationDate());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }
}
