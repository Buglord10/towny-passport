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
    private final Economy economy;
    private final TownyHook townyHook;
    private final PassportStore store;

    public PassportService(TownyPassportPlugin plugin, Economy economy, TownyHook townyHook, PassportStore store) {
        this.plugin = plugin;
        this.economy = economy;
        this.townyHook = townyHook;
        this.store = store;
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

        double defaultFee = documentType == PassportRecord.DocumentType.PASSPORT ? 250.0 : 50.0;
        double fee = plugin.getConfig().getDouble(documentType == PassportRecord.DocumentType.PASSPORT ? "fees.passport" : "fees.visa", defaultFee);
        if (!charge(payer, fee)) {
            return null;
        }

        Instant now = Instant.now();
        int validDays = plugin.getConfig().getInt(documentType == PassportRecord.DocumentType.PASSPORT ? "passport-valid-days" : "visa-valid-days", 90);
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

    public PassportRecord approveApplication(String appId, Player approver) {
        PassportApplication app = store.getApplications().get(appId);
        if (app == null) {
            return null;
        }

        OfflinePlayer target = plugin.getServer().getOfflinePlayer(app.getApplicant());
        Player payer = plugin.getConfig().getBoolean("fees.charge-on-approval", false) ? approver : null;

        PassportRecord record = issueDocument(
                app.getDocumentType(),
                target,
                app.getApplicantName(),
                app.getAge(),
                app.getSex(),
                app.getNotes(),
                app.getAuthorityType(),
                app.getAuthorityName(),
                payer
        );

        if (record != null) {
            store.removeApplication(appId);
            store.save();
        }
        return record;
    }

    public PassportApplication getApplication(String appId) {
        return store.getApplications().get(appId);
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
        EconomyResponse response = economy.withdrawPlayer(player, amount);
        return response.transactionSuccess();
    }
}
