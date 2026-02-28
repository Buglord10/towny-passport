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

        PassportRecord record = service.approveApplication(args[1], player);
        if (record == null) {
            player.sendMessage(ChatColor.RED + "Could not approve application.");
            return;
        }

        player.sendMessage(ChatColor.GREEN + "Approved application. Issued " + record.getDocumentId());
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
        if (!player.hasPermission("townypassport.admin")) {
            player.sendMessage(ChatColor.RED + "Only admins can renew documents.");
            return;
        }
        if (args.length < 3) {
            player.sendMessage(ChatColor.YELLOW + "Usage: /passport renew <documentId> <days>");
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
        if (!player.hasPermission("townypassport.admin")) {
            player.sendMessage(ChatColor.RED + "Only admins can revoke documents.");
            return;
        }
        if (args.length < 2) {
            player.sendMessage(ChatColor.YELLOW + "Usage: /passport revoke <documentId>");
            return;
        }
        if (!service.revokeDocument(args[1])) {
            player.sendMessage(ChatColor.RED + "Document not found.");
            return;
        }
        player.sendMessage(ChatColor.GREEN + "Revoked document " + args[1]);
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
        player.sendMessage(ChatColor.YELLOW + "/passport renew <documentId> <days> (admin)");
        player.sendMessage(ChatColor.YELLOW + "/passport revoke <documentId> (admin)");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> list = new ArrayList<>();
        if (args.length == 1) {
            list.add("apply");
            list.add("issue");
            list.add("applications");
            list.add("approve");
            list.add("view");
            list.add("list");
            list.add("search");
            list.add("renew");
            list.add("revoke");
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("apply") || args[0].equalsIgnoreCase("issue") || args[0].equalsIgnoreCase("applications"))) {
            list.add("town");
            list.add("nation");
        }
        return list;
    }
}
