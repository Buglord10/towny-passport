package com.townypassport;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

public class PassportService {

    private final TownyPassportPlugin plugin;
    private Economy economy;
    private final TownyHook townyHook;
    private final PassportStore store;

    public PassportService(TownyPassportPlugin plugin, Economy economy, TownyHook townyHook, PassportStore store) {
        this.plugin = plugin;
        this.economy = economy;
        this.townyHook = townyHook;
        this.store = store;
    }

    public void updateEconomyProvider(Economy economy) {
        this.economy = economy;
    }

    public boolean hasPendingApplication(UUID applicantId) {
        return store.hasPendingApplication(applicantId);
    }

    public PassportApplication createApplication(
            Player applicant,
            PassportRecord.DocumentType documentType,
            PassportRecord.AuthorityType authorityType,
            String authorityName,
            int age,
            String sex,
            String notes
    ) {
        if (!authorityExists(authorityType, authorityName)) {
            return null;
        }

        if (store.hasPendingApplication(applicant.getUniqueId())) {
            return null;
        }

        String appId = "APP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
        PassportApplication app = new PassportApplication(
                appId,
                applicant.getUniqueId(),
                applicant.getName() == null ? "Unknown" : applicant.getName(),
                documentType,
                authorityType,
                authorityName,
                age,
                sex,
                notes,
                Instant.now()
        );
        store.addApplication(app);
        store.save();

        OfflinePlayer owner = townyHook.resolveAuthorityOwner(authorityType, authorityName);
        if (owner != null && owner.isOnline() && owner.getPlayer() != null) {
            owner.getPlayer().sendMessage("[TownyPassport] New " + documentType.name().toLowerCase(Locale.ROOT)
                    + " application " + appId + " from " + app.getApplicantName() + " for " + authorityName + ".");
        }
        return app;
    }

    public PassportRecord issueDocument(
            PassportRecord.DocumentType documentType,
            OfflinePlayer target,
            String holderName,
            int age,
            String sex,
            String notes,
            PassportRecord.AuthorityType authorityType,
            String authorityName,
            Player payer
    ) {
        if (!authorityExists(authorityType, authorityName)) {
            return null;
        }

        double fee = getConfiguredFee(authorityType, authorityName, documentType);
        if (!charge(payer, fee)) {
            return null;
        }

        Instant now = Instant.now();
        int validDays = getConfiguredDays(authorityType, authorityName, documentType);
        String prefix = documentType == PassportRecord.DocumentType.PASSPORT ? "PP-" : "VS-";
        String docId = prefix + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);

        PassportRecord record = new PassportRecord(
                docId,
                target.getUniqueId(),
                holderName,
                age,
                sex,
                notes,
                authorityType,
                authorityName,
                documentType,
                now,
                now.plus(validDays, ChronoUnit.DAYS)
        );

        store.addPassport(record);
        store.save();
        return record;
    }

    public ApprovalOutcome approveApplication(String appId, Player approver) {
        PassportApplication app = store.getApplications().get(appId);
        if (app == null) {
            return null;
        }

        OfflinePlayer target = plugin.getServer().getOfflinePlayer(app.getApplicant());

        PassportRecord record = issueDocument(
                app.getDocumentType(),
                target,
                app.getApplicantName(),
                app.getAge(),
                app.getSex(),
                app.getNotes(),
                app.getAuthorityType(),
                app.getAuthorityName(),
                null
        );

        if (record == null) {
            return null;
        }

        store.removeApplication(appId);
        store.save();

        double charged = 0.0;
        String beneficiaryName = "None";

        if (plugin.getConfig().getBoolean("fees.charge-on-approval", true) && economy != null) {
            double fee = getConfiguredFee(app.getAuthorityType(), app.getAuthorityName(), app.getDocumentType());

            if (fee > 0) {
                OfflinePlayer beneficiary = townyHook.resolveAuthorityOwner(app.getAuthorityType(), app.getAuthorityName());
                if (beneficiary != null) {
                    EconomyResponse withdraw = economy.withdrawPlayer(target, fee);
                    if (withdraw.transactionSuccess()) {
                        EconomyResponse deposit = economy.depositPlayer(beneficiary, fee);
                        if (deposit.transactionSuccess()) {
                            charged = fee;
                            beneficiaryName = beneficiary.getName() == null ? beneficiary.getUniqueId().toString() : beneficiary.getName();
                        } else {
                            economy.depositPlayer(target, fee);
                        }
                    }
                }
            }
        }

        if (target.isOnline() && target.getPlayer() != null) {
            target.getPlayer().sendMessage("[TownyPassport] Your application was approved. Document: " + record.getDocumentId());
            target.getPlayer().sendMessage("[TownyPassport] Charged: " + charged + " -> " + beneficiaryName);
        }

        OfflinePlayer beneficiaryNotify = townyHook.resolveAuthorityOwner(app.getAuthorityType(), app.getAuthorityName());
        if (beneficiaryNotify != null && beneficiaryNotify.isOnline() && beneficiaryNotify.getPlayer() != null) {
            beneficiaryNotify.getPlayer().sendMessage("[TownyPassport] You received " + charged + " from approved application " + appId + ".");
        }

        return new ApprovalOutcome(record, charged, beneficiaryName);
    }

    public PassportApplication getApplication(String appId) {
        return store.getApplications().get(appId);
    }


    public List<String> getPassportApplicationIds() {
        return store.getApplicationIdsByDocumentType(PassportRecord.DocumentType.PASSPORT);
    }

    public List<String> getVisaApplicationIds() {
        return store.getApplicationIdsByDocumentType(PassportRecord.DocumentType.VISA);
    }

    public List<String> getPassportDocumentIds() {
        return store.getDocumentIdsByType(PassportRecord.DocumentType.PASSPORT);
    }

    public List<String> getVisaDocumentIds() {
        return store.getDocumentIdsByType(PassportRecord.DocumentType.VISA);
    }

    public List<String> getAllDocumentIds() {
        return store.getAllDocumentIds();
    }

    public List<PassportRecord> getPassports(UUID playerId) {
        return store.getPassports(playerId);
    }

    public PassportRecord findDocument(String documentId) {
        return store.findDocumentById(documentId);
    }

    public boolean revokeDocument(String documentId) {
        PassportRecord removed = store.removeDocumentById(documentId);
        if (removed == null) {
            return false;
        }
        store.save();
        return true;
    }

    public PassportRecord renewDocument(String documentId, int days) {
        PassportRecord existing = store.findDocumentById(documentId);
        if (existing == null || days <= 0) {
            return null;
        }
        Instant base = existing.getExpiresAt().isAfter(Instant.now()) ? existing.getExpiresAt() : Instant.now();
        PassportRecord updated = existing.withExpiry(base.plus(days, ChronoUnit.DAYS));
        if (!store.replaceDocument(updated)) {
            return null;
        }
        store.save();
        return updated;
    }

    public void ensureTownOwnerStarterPassport(Player player) {
        if (!plugin.getConfig().getBoolean("starter-passport-town-owner.enabled", true)) {
            return;
        }

        String townName = townyHook.getTownForPlayer(player);
        if (townName == null || !townyHook.isTownOwner(player, townName)) {
            return;
        }

        boolean hasTownPassport = getPassports(player.getUniqueId()).stream()
                .anyMatch(doc -> doc.getDocumentType() == PassportRecord.DocumentType.PASSPORT
                        && doc.getAuthorityType() == PassportRecord.AuthorityType.TOWN
                        && doc.getAuthorityName().equalsIgnoreCase(townName));

        if (hasTownPassport) {
            return;
        }

        int defaultAge = plugin.getConfig().getInt("starter-passport-town-owner.default-age", 18);
        String defaultSex = plugin.getConfig().getString("starter-passport-town-owner.default-sex", "Unspecified");
        String defaultNotes = plugin.getConfig().getString("starter-passport-town-owner.default-notes", "Issued automatically to town owner");
        if (defaultSex == null || defaultSex.isBlank()) defaultSex = "Unspecified";
        if (defaultNotes == null || defaultNotes.isBlank()) defaultNotes = "Issued automatically to town owner";

        issueDocument(
                PassportRecord.DocumentType.PASSPORT,
                player,
                player.getName() == null ? "Unknown" : player.getName(),
                defaultAge,
                defaultSex,
                defaultNotes,
                PassportRecord.AuthorityType.TOWN,
                townName,
                null
        );
    }



    public void setAuthoritySetting(PassportRecord.AuthorityType type, String authorityName, String setting, String value) {
        String key = "authority-settings." + authorityKey(type, authorityName) + "." + setting;
        plugin.getConfig().set(key, value);
        plugin.saveConfig();
    }

    public String getAuthoritySettingsSummary(PassportRecord.AuthorityType type, String authorityName) {
        double passportFee = getConfiguredFee(type, authorityName, PassportRecord.DocumentType.PASSPORT);
        double visaFee = getConfiguredFee(type, authorityName, PassportRecord.DocumentType.VISA);
        int passportDays = getConfiguredDays(type, authorityName, PassportRecord.DocumentType.PASSPORT);
        int visaDays = getConfiguredDays(type, authorityName, PassportRecord.DocumentType.VISA);
        return "passportFee=" + passportFee + ", visaFee=" + visaFee + ", passportDays=" + passportDays + ", visaDays=" + visaDays;
    }

    private String authorityKey(PassportRecord.AuthorityType type, String authorityName) {
        return type.name().toLowerCase(Locale.ROOT) + "." + authorityName.toLowerCase(Locale.ROOT);
    }

    private double getConfiguredFee(PassportRecord.AuthorityType authorityType, String authorityName, PassportRecord.DocumentType documentType) {
        String overrideKey = "authority-settings." + authorityKey(authorityType, authorityName) + "." + (documentType == PassportRecord.DocumentType.PASSPORT ? "passport-fee" : "visa-fee");
        if (plugin.getConfig().contains(overrideKey)) {
            return plugin.getConfig().getDouble(overrideKey);
        }
        double defaultFee = documentType == PassportRecord.DocumentType.PASSPORT ? 250.0 : 50.0;
        return plugin.getConfig().getDouble(documentType == PassportRecord.DocumentType.PASSPORT ? "fees.passport" : "fees.visa", defaultFee);
    }

    private int getConfiguredDays(PassportRecord.AuthorityType authorityType, String authorityName, PassportRecord.DocumentType documentType) {
        String overrideKey = "authority-settings." + authorityKey(authorityType, authorityName) + "." + (documentType == PassportRecord.DocumentType.PASSPORT ? "passport-days" : "visa-days");
        if (plugin.getConfig().contains(overrideKey)) {
            return plugin.getConfig().getInt(overrideKey);
        }
        return plugin.getConfig().getInt(documentType == PassportRecord.DocumentType.PASSPORT ? "passport-valid-days" : "visa-valid-days", 90);
    }

    public List<PassportApplication> getApplicationsForAuthority(PassportRecord.AuthorityType type, String authorityName) {
        return store.getApplications().values().stream()
                .filter(app -> app.getAuthorityType() == type && app.getAuthorityName().equalsIgnoreCase(authorityName))
                .collect(Collectors.toList());
    }

    public boolean canEnterTown(UUID playerId, String townName) {
        String townNation = townyHook.getNationForTown(townName);
        Instant now = Instant.now();

        for (PassportRecord record : store.getPassports(playerId)) {
            if (!record.isValidAt(now) || record.getDocumentType() != PassportRecord.DocumentType.PASSPORT) {
                continue;
            }
            if (passportAllowsTownEntry(record, townName, townNation)) {
                return true;
            }
        }
        return false;
    }

    private boolean passportAllowsTownEntry(PassportRecord record, String townName, String townNation) {
        if (record.getAuthorityType() == PassportRecord.AuthorityType.TOWN) {
            return record.getAuthorityName().equalsIgnoreCase(townName);
        }
        return townNation != null && record.getAuthorityName().equalsIgnoreCase(townNation);
    }

    private boolean authorityExists(PassportRecord.AuthorityType type, String authorityName) {
        if (type == PassportRecord.AuthorityType.TOWN) {
            return townyHook.townExists(authorityName);
        }
        return townyHook.nationExists(authorityName);
    }

    private boolean charge(Player player, double amount) {
        if (amount <= 0 || player == null) {
            return true;
        }
        if (economy == null) {
            return true;
        }
        EconomyResponse response = economy.withdrawPlayer(player, amount);
        return response.transactionSuccess();
    }
}
