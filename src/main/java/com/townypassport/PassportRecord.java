package com.townypassport;

import java.time.Instant;
import java.util.UUID;

public class PassportRecord {

    public enum AuthorityType {
        TOWN,
        NATION
    }

    public enum DocumentType {
        PASSPORT,
        VISA
    }

    private final String documentId;
    private final UUID owner;
    private final String holderName;
    private final int age;
    private final String sex;
    private final String notes;
    private final AuthorityType authorityType;
    private final String authorityName;
    private final DocumentType documentType;
    private final Instant issuedAt;
    private final Instant expiresAt;

    public PassportRecord(
            String documentId,
            UUID owner,
            String holderName,
            int age,
            String sex,
            String notes,
            AuthorityType authorityType,
            String authorityName,
            DocumentType documentType,
            Instant issuedAt,
            Instant expiresAt
    ) {
        this.documentId = documentId;
        this.owner = owner;
        this.holderName = holderName;
        this.age = age;
        this.sex = sex;
        this.notes = notes;
        this.authorityType = authorityType;
        this.authorityName = authorityName;
        this.documentType = documentType;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
    }

    public String getDocumentId() { return documentId; }
    public UUID getOwner() { return owner; }
    public String getHolderName() { return holderName; }
    public int getAge() { return age; }
    public String getSex() { return sex; }
    public String getNotes() { return notes; }
    public AuthorityType getAuthorityType() { return authorityType; }
    public String getAuthorityName() { return authorityName; }
    public DocumentType getDocumentType() { return documentType; }
    public Instant getIssuedAt() { return issuedAt; }
    public Instant getExpiresAt() { return expiresAt; }

    public boolean isValidAt(Instant time) {
        return expiresAt.isAfter(time);
    }
}
