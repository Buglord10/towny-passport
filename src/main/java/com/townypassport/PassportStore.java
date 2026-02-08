package com.townypassport;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class PassportStore {

    private final JavaPlugin plugin;
    private final File dataFile;
    private final Map<UUID, List<PassportRecord>> passportsByPlayer = new HashMap<>();
    private final Map<String, PassportApplication> applications = new HashMap<>();

    public PassportStore(JavaPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "passports.yml");
    }

    public List<PassportRecord> getPassports(UUID uuid) {
        return passportsByPlayer.getOrDefault(uuid, new ArrayList<>());
    }

    public void addPassport(PassportRecord passport) {
        passportsByPlayer.computeIfAbsent(passport.getOwner(), ignored -> new ArrayList<>()).add(passport);
    }

    public Map<String, PassportApplication> getApplications() {
        return applications;
    }

    public void addApplication(PassportApplication application) {
        applications.put(application.getApplicationId(), application);
    }

    public PassportApplication removeApplication(String id) {
        return applications.remove(id);
    }

    public void load() {
        if (!dataFile.exists()) {
            return;
        }

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(dataFile);

        ConfigurationSection passportsRoot = yaml.getConfigurationSection("passports");
        if (passportsRoot != null) {
            for (String uuidKey : passportsRoot.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidKey);
                    ConfigurationSection playerSection = passportsRoot.getConfigurationSection(uuidKey);
                    if (playerSection == null) {
                        continue;
                    }

                    List<PassportRecord> records = new ArrayList<>();
                    for (String docId : playerSection.getKeys(false)) {
                        ConfigurationSection doc = playerSection.getConfigurationSection(docId);
                        if (doc == null) {
                            continue;
                        }
                        records.add(new PassportRecord(
                                docId,
                                uuid,
                                doc.getString("holderName", "Unknown"),
                                doc.getInt("age", 18),
                                doc.getString("sex", "Unspecified"),
                                doc.getString("notes", "None"),
                                PassportRecord.AuthorityType.valueOf(doc.getString("authorityType", "TOWN")),
                                doc.getString("authorityName", "Unknown"),
                                PassportRecord.DocumentType.valueOf(doc.getString("documentType", "PASSPORT")),
                                Instant.ofEpochMilli(doc.getLong("issuedAt", System.currentTimeMillis())),
                                Instant.ofEpochMilli(doc.getLong("expiresAt", System.currentTimeMillis()))
                        ));
                    }
                    passportsByPlayer.put(uuid, records);
                } catch (Exception ex) {
                    plugin.getLogger().warning("Failed loading passports for " + uuidKey + ": " + ex.getMessage());
                }
            }
        }

        ConfigurationSection appsRoot = yaml.getConfigurationSection("applications");
        if (appsRoot == null) {
            return;
        }

        for (String appId : appsRoot.getKeys(false)) {
            ConfigurationSection app = appsRoot.getConfigurationSection(appId);
            if (app == null) {
                continue;
            }

            try {
                PassportApplication application = new PassportApplication(
                        appId,
                        UUID.fromString(app.getString("applicant")),
                        app.getString("applicantName", "Unknown"),
                        PassportRecord.DocumentType.valueOf(app.getString("documentType", "PASSPORT").toUpperCase(Locale.ROOT)),
                        PassportRecord.AuthorityType.valueOf(app.getString("authorityType", "TOWN").toUpperCase(Locale.ROOT)),
                        app.getString("authorityName", "Unknown"),
                        app.getInt("age", 18),
                        app.getString("sex", "Unspecified"),
                        app.getString("notes", "None"),
                        Instant.ofEpochMilli(app.getLong("createdAt", System.currentTimeMillis()))
                );
                applications.put(appId, application);
            } catch (Exception ex) {
                plugin.getLogger().warning("Failed loading application " + appId + ": " + ex.getMessage());
            }
        }
    }

    public void save() {
        if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {
            plugin.getLogger().warning("Failed creating plugin data folder.");
            return;
        }

        YamlConfiguration yaml = new YamlConfiguration();

        for (Map.Entry<UUID, List<PassportRecord>> entry : passportsByPlayer.entrySet()) {
            String base = "passports." + entry.getKey();
            for (PassportRecord doc : entry.getValue()) {
                String docBase = base + "." + doc.getDocumentId();
                yaml.set(docBase + ".holderName", doc.getHolderName());
                yaml.set(docBase + ".age", doc.getAge());
                yaml.set(docBase + ".sex", doc.getSex());
                yaml.set(docBase + ".notes", doc.getNotes());
                yaml.set(docBase + ".authorityType", doc.getAuthorityType().name());
                yaml.set(docBase + ".authorityName", doc.getAuthorityName());
                yaml.set(docBase + ".documentType", doc.getDocumentType().name());
                yaml.set(docBase + ".issuedAt", doc.getIssuedAt().toEpochMilli());
                yaml.set(docBase + ".expiresAt", doc.getExpiresAt().toEpochMilli());
            }
        }

        for (PassportApplication app : applications.values()) {
            String base = "applications." + app.getApplicationId();
            yaml.set(base + ".applicant", app.getApplicant().toString());
            yaml.set(base + ".applicantName", app.getApplicantName());
            yaml.set(base + ".documentType", app.getDocumentType().name());
            yaml.set(base + ".authorityType", app.getAuthorityType().name());
            yaml.set(base + ".authorityName", app.getAuthorityName());
            yaml.set(base + ".age", app.getAge());
            yaml.set(base + ".sex", app.getSex());
            yaml.set(base + ".notes", app.getNotes());
            yaml.set(base + ".createdAt", app.getCreatedAt().toEpochMilli());
        }

        try {
            yaml.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save passports.yml: " + e.getMessage());
        }
    }
}
