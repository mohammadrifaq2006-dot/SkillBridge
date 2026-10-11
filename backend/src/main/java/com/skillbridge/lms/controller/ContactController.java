package com.skillbridge.lms.controller;

import com.skillbridge.lms.dto.ContactInquiryRequest;
import com.skillbridge.lms.dto.ContactInquiryResponse;
import com.skillbridge.lms.service.ContactService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contact")
@Tag(name = "Contact Inquiries", description = "Public endpoint for submitting contact inquiries")
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @PostMapping
    @Operation(summary = "Submit a contact inquiry and store it in MySQL")
    public ResponseEntity<ContactInquiryResponse> submitInquiry(@Valid @RequestBody ContactInquiryRequest request) {
        ContactInquiryResponse saved = contactService.submitInquiry(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
}
