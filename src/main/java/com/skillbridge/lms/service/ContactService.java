package com.skillbridge.lms.service;

import com.skillbridge.lms.dto.ContactInquiryRequest;
import com.skillbridge.lms.dto.ContactInquiryResponse;
import com.skillbridge.lms.entity.ContactInquiry;
import com.skillbridge.lms.repository.ContactInquiryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ContactService {

    private static final Logger log = LoggerFactory.getLogger(ContactService.class);

    private final ContactInquiryRepository contactInquiryRepository;

    public ContactService(ContactInquiryRepository contactInquiryRepository) {
        this.contactInquiryRepository = contactInquiryRepository;
    }

    @Transactional
    public ContactInquiryResponse submitInquiry(ContactInquiryRequest request) {
        ContactInquiry inquiry = new ContactInquiry(
                request.getName().trim(),
                request.getEmail().toLowerCase().trim(),
                request.getSubject().trim(),
                request.getMessage().trim()
        );
        ContactInquiry saved = contactInquiryRepository.save(inquiry);
        log.info("Saved contact inquiry ID={} from '{}'", saved.getId(), saved.getEmail());
        return ContactInquiryResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public List<ContactInquiryResponse> getAllInquiries() {
        return contactInquiryRepository.findAllByOrderByCreationDateDesc()
                .stream()
                .map(ContactInquiryResponse::fromEntity)
                .toList();
    }
}
