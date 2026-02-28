package com.townypassport;

public class ApprovalOutcome {

    private final PassportRecord record;
    private final double chargedAmount;
    private final String beneficiaryName;

    public ApprovalOutcome(PassportRecord record, double chargedAmount, String beneficiaryName) {
        this.record = record;
        this.chargedAmount = chargedAmount;
        this.beneficiaryName = beneficiaryName;
    }

    public PassportRecord getRecord() {
        return record;
    }

    public double getChargedAmount() {
        return chargedAmount;
    }

    public String getBeneficiaryName() {
        return beneficiaryName;
    }
}
