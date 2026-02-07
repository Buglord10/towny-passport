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

        double fee = plugin.getConfig().getDouble(documentType == PassportRecord.DocumentType.PASSPORT ? "fees.passport" : "fees.visa", 250.0);
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

    public PassportRecord approveApplication(String appId, Player payer) {
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
            // Town passport allows entry only to that specific town.
            return record.getAuthorityName().equalsIgnoreCase(townName);
        }

        // Nation passport allows entry to any town inside that nation.
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
