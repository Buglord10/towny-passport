package com.townypassport;

import java.time.Instant;
import java.util.UUID;

public class PassportApplication {

    private final String applicationId;
    private final UUID applicant;
    private final String applicantName;
    private final PassportRecord.DocumentType documentType;
    private final PassportRecord.AuthorityType authorityType;
    private final String authorityName;
    private final int age;
    private final String sex;
    private final String notes;
    private final Instant createdAt;

    public PassportApplication(
            String applicationId,
            UUID applicant,
            String applicantName,
            PassportRecord.DocumentType documentType,
            PassportRecord.AuthorityType authorityType,
            String authorityName,
            int age,
            String sex,
            String notes,
            Instant createdAt
    ) {
        this.applicationId = applicationId;
        this.applicant = applicant;
        this.applicantName = applicantName;
        this.documentType = documentType;
        this.authorityType = authorityType;
        this.authorityName = authorityName;
        this.age = age;
        this.sex = sex;
        this.notes = notes;
        this.createdAt = createdAt;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public UUID getApplicant() {
        return applicant;
    }

    public String getApplicantName() {
        return applicantName;
    }

    public PassportRecord.DocumentType getDocumentType() {
        return documentType;
    }

    public PassportRecord.AuthorityType getAuthorityType() {
        return authorityType;
    }

    public String getAuthorityName() {
        return authorityName;
    }

    public int getAge() {
        return age;
    }

    public String getSex() {
        return sex;
    }

    public String getNotes() {
        return notes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
