package com.townypassport;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class PassportCommand implements CommandExecutor, TabCompleter {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm")
            .withZone(ZoneId.systemDefault());

    private final PassportService service;
    private final TownyHook townyHook;
    private final PortraitIntegration portraitIntegration;

    public PassportCommand(PassportService service, TownyHook townyHook, PortraitIntegration portraitIntegration) {
        this.service = service;
        this.townyHook = townyHook;
        this.portraitIntegration = portraitIntegration;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "This command is player-only.");
            return true;
        }

        if (args.length == 0) {
            sendUsage(player);
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "apply" -> handleApply(player, args);
            case "issue" -> handleIssue(player, args, PassportRecord.DocumentType.PASSPORT);
            case "approve" -> handleApprove(player, args);
            case "applications" -> handleApplications(player, args);
            case "view" -> handleView(player, args);
            case "list" -> handleList(player, args);
            case "search" -> handleSearch(player, args);
            case "renew" -> handleRenew(player, args);
            case "revoke" -> handleRevoke(player, args);
            case "settings" -> handleSettings(player, args);
            default -> sendUsage(player);
        }
        return true;
    }

    private void handleApply(Player player, String[] args) {
        if (args.length < 5) {
            player.sendMessage(ChatColor.YELLOW + "Usage: /passport apply <town|nation> <authorityName> <age> <sex> [notes]");
            return;
        }

        PassportRecord.AuthorityType type = parseAuthority(args[1]);
        if (type == null) {
            player.sendMessage(ChatColor.RED + "Authority type must be town or nation.");
            return;
        }

        int age = parseAge(args[3]);
        if (age < 0) {
            player.sendMessage(ChatColor.RED + "Age must be a positive number.");
            return;
        }

        String notes = args.length > 5 ? String.join(" ", Arrays.copyOfRange(args, 5, args.length)) : "None";
        if (service.hasPendingApplication(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You already have a pending application. Wait for approval before applying again.");
            return;
        }

        PassportApplication app = service.createApplication(player, PassportRecord.DocumentType.PASSPORT, type, args[2], age, args[4], notes);
        if (app == null) {
            player.sendMessage(ChatColor.RED + "Target authority does not exist in Towny.");
            return;
        }

        player.sendMessage(ChatColor.GREEN + "Application submitted: " + app.getApplicationId());
    }

    private void handleIssue(Player player, String[] args, PassportRecord.DocumentType docType) {
        if (args.length < 6) {
            player.sendMessage(ChatColor.YELLOW + "Usage: /passport issue <player> <town|nation> <authorityName> <age> <sex> [notes]");
            return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        PassportRecord.AuthorityType type = parseAuthority(args[2]);
        if (type == null) {
            player.sendMessage(ChatColor.RED + "Authority type must be town or nation.");
            return;
        }

        String authorityName = args[3];
        if (!canIssue(player, type, authorityName)) {
            player.sendMessage(ChatColor.RED + "You cannot issue documents for this authority.");
            return;
        }

        int age = parseAge(args[4]);
        if (age < 0) {
            player.sendMessage(ChatColor.RED + "Age must be a positive number.");
            return;
        }

        String notes = args.length > 6 ? String.join(" ", Arrays.copyOfRange(args, 6, args.length)) : "None";
        String holderName = target.getName() == null ? args[1] : target.getName();

        PassportRecord record = service.issueDocument(docType, target, holderName, age, args[5], notes, type, authorityName, player);
        if (record == null) {
            player.sendMessage(ChatColor.RED + "Issue failed (bad authority or insufficient balance).");
            return;
        }

        player.sendMessage(ChatColor.GREEN + "Issued " + record.getDocumentType().name().toLowerCase(Locale.ROOT)
                + " " + record.getDocumentId() + " to " + holderName + ".");
    }

    private void handleApprove(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.YELLOW + "Usage: /passport approve <applicationId>");
            return;
        }

        PassportApplication app = service.getApplication(args[1]);
        if (app == null || app.getDocumentType() != PassportRecord.DocumentType.PASSPORT) {
            player.sendMessage(ChatColor.RED + "Passport application not found.");
            return;
        }

        if (!canIssue(player, app.getAuthorityType(), app.getAuthorityName())) {
            player.sendMessage(ChatColor.RED + "You cannot approve applications for this authority.");
            return;
        }

        ApprovalOutcome outcome = service.approveApplication(args[1], player);
        if (outcome == null || outcome.getRecord() == null) {
            player.sendMessage(ChatColor.RED + "Could not approve application.");
            return;
        }

        PassportRecord record = outcome.getRecord();
        player.sendMessage(ChatColor.GREEN + "Approved application. Issued " + record.getDocumentId());
        player.sendMessage(ChatColor.AQUA + "Charge on approval: " + outcome.getChargedAmount() + " paid to " + outcome.getBeneficiaryName());
    }

    private void handleApplications(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(ChatColor.YELLOW + "Usage: /passport applications <town|nation> <authorityName>");
            return;
        }

        PassportRecord.AuthorityType type = parseAuthority(args[1]);
        if (type == null) {
            player.sendMessage(ChatColor.RED + "Authority type must be town or nation.");
            return;
        }

        if (!canIssue(player, type, args[2])) {
            player.sendMessage(ChatColor.RED + "You cannot review this authority's applications.");
            return;
        }

        List<PassportApplication> applications = service.getApplicationsForAuthority(type, args[2]);
        if (applications.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "No applications found.");
            return;
        }

        player.sendMessage(ChatColor.GOLD + "Applications for " + args[2] + ":");
        for (PassportApplication app : applications) {
            player.sendMessage(ChatColor.GRAY + "- " + app.getApplicationId() + " | " + app.getApplicantName()
                    + " | " + app.getDocumentType().name().toLowerCase(Locale.ROOT));
        }
    }

    private void handleView(Player player, String[] args) {
        OfflinePlayer target = args.length > 1 ? Bukkit.getOfflinePlayer(args[1]) : player;
        List<PassportRecord> docs = service.getPassports(target.getUniqueId());
        if (docs.isEmpty()) {
            player.sendMessage(ChatColor.RED + "No documents found.");
            return;
        }

        int index = 0;
        if (args.length > 2) {
            try {
                index = Integer.parseInt(args[2]) - 1;
            } catch (NumberFormatException ignored) {
                player.sendMessage(ChatColor.RED + "Index must be a number.");
                return;
            }
        }

        if (index < 0 || index >= docs.size()) {
            player.sendMessage(ChatColor.RED + "Invalid index. Range: 1-" + docs.size());
            return;
        }

        PassportRecord doc = docs.get(index);
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        if (meta == null) {
            return;
        }

        meta.setTitle(doc.getDocumentType().name() + " " + doc.getDocumentId());
        meta.setAuthor(doc.getAuthorityName());
        String portraitLine = portraitIntegration.resolvePortraitLine(target);
        String portraitSection = portraitLine.isBlank() ? "" : portraitLine + "\n\n";

        meta.addPage(
                portraitSection + ChatColor.GOLD + "Document ID: " + doc.getDocumentId() + "\n\n"
                        + ChatColor.BLACK + "Type: " + doc.getDocumentType().name() + "\n"
                        + "Name: " + doc.getHolderName() + "\n"
                        + "Age: " + doc.getAge() + "\n"
                        + "Sex: " + doc.getSex() + "\n"
                        + "Authority: " + doc.getAuthorityType().name() + " " + doc.getAuthorityName() + "\n"
                        + "Issued: " + FORMATTER.format(doc.getIssuedAt()) + "\n"
                        + "Expires: " + FORMATTER.format(doc.getExpiresAt()) + "\n"
                        + "Notes: " + doc.getNotes()
        );
        book.setItemMeta(meta);
        player.openBook(book);
        player.sendMessage(ChatColor.GREEN + "Opened document " + (index + 1) + " of " + docs.size() + ".");
    }

    private void handleList(Player player, String[] args) {
        OfflinePlayer target = args.length > 1 ? Bukkit.getOfflinePlayer(args[1]) : player;
        List<PassportRecord> docs = service.getPassports(target.getUniqueId());
        if (docs.isEmpty()) {
            player.sendMessage(ChatColor.RED + "No documents found for " + target.getName());
            return;
        }
        player.sendMessage(ChatColor.GOLD + "Documents for " + target.getName() + ":");
        for (PassportRecord doc : docs) {
            player.sendMessage(ChatColor.GRAY + "- " + doc.getDocumentId() + " | " + doc.getDocumentType().name()
                    + " | " + doc.getAuthorityType().name() + " " + doc.getAuthorityName()
                    + " | expires " + FORMATTER.format(doc.getExpiresAt()));
        }
    }

    private void handleSearch(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.YELLOW + "Usage: /passport search <documentId>");
            return;
        }
        PassportRecord doc = service.findDocument(args[1]);
        if (doc == null) {
            player.sendMessage(ChatColor.RED + "Document not found.");
            return;
        }
        player.sendMessage(ChatColor.GREEN + "Found " + doc.getDocumentId() + " for " + doc.getHolderName()
                + " (" + doc.getAuthorityType().name() + " " + doc.getAuthorityName() + ")");
    }

    private void handleRenew(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(ChatColor.YELLOW + "Usage: /passport renew <documentId> <days>");
            return;
        }
        PassportRecord existing = service.findDocument(args[1]);
        if (existing == null) {
            player.sendMessage(ChatColor.RED + "Document not found.");
            return;
        }
        if (!canIssue(player, existing.getAuthorityType(), existing.getAuthorityName())) {
            player.sendMessage(ChatColor.RED + "Only the owning town/nation leader can renew this document.");
            return;
        }
        int days = parseAge(args[2]);
        if (days <= 0) {
            player.sendMessage(ChatColor.RED + "Days must be a positive number.");
            return;
        }
        PassportRecord updated = service.renewDocument(args[1], days);
        if (updated == null) {
            player.sendMessage(ChatColor.RED + "Could not renew document.");
            return;
        }
        player.sendMessage(ChatColor.GREEN + "Renewed " + updated.getDocumentId() + " until " + FORMATTER.format(updated.getExpiresAt()));
    }

    private void handleRevoke(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.YELLOW + "Usage: /passport revoke <documentId>");
            return;
        }
        PassportRecord existing = service.findDocument(args[1]);
        if (existing == null) {
            player.sendMessage(ChatColor.RED + "Document not found.");
            return;
        }
        if (!canIssue(player, existing.getAuthorityType(), existing.getAuthorityName())) {
            player.sendMessage(ChatColor.RED + "Only the owning town/nation leader can revoke this document.");
            return;
        }
        if (!service.revokeDocument(args[1])) {
            player.sendMessage(ChatColor.RED + "Document not found.");
            return;
        }
        player.sendMessage(ChatColor.GREEN + "Revoked document " + args[1]);
    }



    private void handleSettings(Player player, String[] args) {
        if (args.length < 4) {
            player.sendMessage(ChatColor.YELLOW + "Usage: /passport settings <town|nation> <authorityName> <show|passport-fee|visa-fee|passport-days|visa-days> [value]");
            return;
        }

        PassportRecord.AuthorityType type = parseAuthority(args[1]);
        if (type == null) {
            player.sendMessage(ChatColor.RED + "Authority type must be town or nation.");
            return;
        }

        String authorityName = args[2];
        if (!canIssue(player, type, authorityName)) {
            player.sendMessage(ChatColor.RED + "You cannot manage settings for this authority.");
            return;
        }

        String setting = args[3].toLowerCase(Locale.ROOT);
        if (setting.equals("show")) {
            player.sendMessage(ChatColor.AQUA + "Settings for " + authorityName + ": " + service.getAuthoritySettingsSummary(type, authorityName));
            return;
        }

        if (args.length < 5) {
            player.sendMessage(ChatColor.YELLOW + "Provide a value for " + setting + ".");
            return;
        }

        String value = args[4];
        try {
            if (setting.endsWith("fee")) {
                double parsed = Double.parseDouble(value);
                if (parsed < 0) throw new NumberFormatException();
            } else if (setting.endsWith("days")) {
                int parsed = Integer.parseInt(value);
                if (parsed <= 0) throw new NumberFormatException();
            } else {
                player.sendMessage(ChatColor.RED + "Unknown setting: " + setting);
                return;
            }
        } catch (NumberFormatException ex) {
            player.sendMessage(ChatColor.RED + "Invalid numeric value.");
            return;
        }

        service.setAuthoritySetting(type, authorityName, setting, value);
        player.sendMessage(ChatColor.GREEN + "Updated " + authorityName + " setting " + setting + " = " + value);
    }

    private PassportRecord.AuthorityType parseAuthority(String input) {
        if (input.equalsIgnoreCase("town")) {
            return PassportRecord.AuthorityType.TOWN;
        }
        if (input.equalsIgnoreCase("nation")) {
            return PassportRecord.AuthorityType.NATION;
        }
        return null;
    }

    private int parseAge(String input) {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    private boolean canIssue(Player player, PassportRecord.AuthorityType type, String authorityName) {
        if (type == PassportRecord.AuthorityType.TOWN) {
            return townyHook.canIssueTownDocuments(player, authorityName);
        }
        return townyHook.canIssueNationDocuments(player, authorityName);
    }

    private void sendUsage(Player player) {
        player.sendMessage(ChatColor.YELLOW + "/passport apply <town|nation> <authorityName> <age> <sex> [notes]");
        player.sendMessage(ChatColor.YELLOW + "/passport issue <player> <town|nation> <authorityName> <age> <sex> [notes]");
        player.sendMessage(ChatColor.YELLOW + "/passport applications <town|nation> <authorityName>");
        player.sendMessage(ChatColor.YELLOW + "/passport approve <applicationId>");
        player.sendMessage(ChatColor.YELLOW + "/passport view [player] [index]");
        player.sendMessage(ChatColor.YELLOW + "/passport list [player]");
        player.sendMessage(ChatColor.YELLOW + "/passport search <documentId>");
        player.sendMessage(ChatColor.YELLOW + "/passport renew <documentId> <days>");
        player.sendMessage(ChatColor.YELLOW + "/passport revoke <documentId>");
        player.sendMessage(ChatColor.YELLOW + "/passport settings <town|nation> <authorityName> <show|passport-fee|visa-fee|passport-days|visa-days> [value]");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        String sub = args.length > 0 ? args[0].toLowerCase(Locale.ROOT) : "";

        if (args.length == 1) {
            return completePrefix(args[0], List.of("apply", "issue", "applications", "approve", "view", "list", "search", "renew", "revoke", "settings"));
        }

        if (sub.equals("apply")) {
            if (args.length == 2) return completePrefix(args[1], List.of("town", "nation"));
            if (args.length == 3) return completePrefix(args[2], args[1].equalsIgnoreCase("town") ? townyHook.getTownNames() : townyHook.getNationNames());
            if (args.length == 4) return completePrefix(args[3], List.of("18", "21", "25", "30"));
            if (args.length == 5) return completePrefix(args[4], List.of("Male", "Female", "Other", "Unspecified"));
        }

        if (sub.equals("issue")) {
            if (args.length == 2) return completePrefix(args[1], Bukkit.getOnlinePlayers().stream().map(Player::getName).toList());
            if (args.length == 3) return completePrefix(args[2], List.of("town", "nation"));
            if (args.length == 4) return completePrefix(args[3], args[2].equalsIgnoreCase("town") ? townyHook.getTownNames() : townyHook.getNationNames());
            if (args.length == 5) return completePrefix(args[4], List.of("18", "21", "25", "30"));
            if (args.length == 6) return completePrefix(args[5], List.of("Male", "Female", "Other", "Unspecified"));
        }

        if (sub.equals("applications")) {
            if (args.length == 2) return completePrefix(args[1], List.of("town", "nation"));
            if (args.length == 3) return completePrefix(args[2], args[1].equalsIgnoreCase("town") ? townyHook.getTownNames() : townyHook.getNationNames());
        }

        if (sub.equals("approve") && args.length == 2) {
            return completePrefix(args[1], service.getPassportApplicationIds());
        }

        if (sub.equals("view")) {
            if (args.length == 2) return completePrefix(args[1], Bukkit.getOnlinePlayers().stream().map(Player::getName).toList());
            if (args.length == 3 && sender instanceof Player player) {
                int count = service.getPassports(player.getUniqueId()).size();
                List<String> indexes = new ArrayList<>();
                for (int i = 1; i <= Math.max(count, 1); i++) indexes.add(Integer.toString(i));
                return completePrefix(args[2], indexes);
            }
        }

        if (sub.equals("list") && args.length == 2) {
            return completePrefix(args[1], Bukkit.getOnlinePlayers().stream().map(Player::getName).toList());
        }

        if (sub.equals("search") && args.length == 2) {
            return completePrefix(args[1], service.getAllDocumentIds());
        }

        if (sub.equals("renew")) {
            if (args.length == 2) return completePrefix(args[1], service.getPassportDocumentIds());
            if (args.length == 3) return completePrefix(args[2], List.of("7", "14", "30", "90"));
        }

        if (sub.equals("revoke") && args.length == 2) {
            return completePrefix(args[1], service.getPassportDocumentIds());
        }

        if (sub.equals("settings")) {
            if (args.length == 2) return completePrefix(args[1], List.of("town", "nation"));
            if (args.length == 3) return completePrefix(args[2], args[1].equalsIgnoreCase("town") ? townyHook.getTownNames() : townyHook.getNationNames());
            if (args.length == 4) return completePrefix(args[3], List.of("show", "passport-fee", "visa-fee", "passport-days", "visa-days"));
            if (args.length == 5 && args[3].toLowerCase(Locale.ROOT).endsWith("fee")) return completePrefix(args[4], List.of("0", "10", "25", "50", "100", "250"));
            if (args.length == 5 && args[3].toLowerCase(Locale.ROOT).endsWith("days")) return completePrefix(args[4], List.of("7", "14", "30", "90", "180"));
        }

        return new ArrayList<>();
    }

    private List<String> completePrefix(String current, List<String> options) {
        String lower = current == null ? "" : current.toLowerCase(Locale.ROOT);
        List<String> out = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase(Locale.ROOT).startsWith(lower)) {
                out.add(option);
            }
        }
        return out;
    }
}
