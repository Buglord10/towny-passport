package com.townypassport;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class VisaCommand implements CommandExecutor, TabCompleter {

    private final PassportService service;
    private final TownyHook townyHook;

    public VisaCommand(PassportService service, TownyHook townyHook) {
        this.service = service;
        this.townyHook = townyHook;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "This command is player-only.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(ChatColor.YELLOW + "/visa apply <town|nation> <authorityName> <age> <sex> [notes]");
            player.sendMessage(ChatColor.YELLOW + "/visa issue <player> <town|nation> <authorityName> <age> <sex> [notes]");
            player.sendMessage(ChatColor.YELLOW + "/visa approve <applicationId>");
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        if (sub.equals("apply")) {
            if (args.length < 5) {
                player.sendMessage(ChatColor.YELLOW + "Usage: /visa apply <town|nation> <authorityName> <age> <sex> [notes]");
                return true;
            }
            PassportRecord.AuthorityType type = parseAuthority(args[1]);
            int age = parseAge(args[3]);
            if (type == null || age < 0) {
                player.sendMessage(ChatColor.RED + "Invalid authority or age.");
                return true;
            }
            if (service.hasPendingApplication(player.getUniqueId())) {
                player.sendMessage(ChatColor.RED + "You already have a pending application. Wait for approval before applying again.");
                return true;
            }
            String notes = args.length > 5 ? String.join(" ", Arrays.copyOfRange(args, 5, args.length)) : "None";
            PassportApplication app = service.createApplication(player, PassportRecord.DocumentType.VISA, type, args[2], age, args[4], notes);
            if (app == null) {
                player.sendMessage(ChatColor.RED + "Authority not found.");
                return true;
            }
            player.sendMessage(ChatColor.GREEN + "Visa application submitted: " + app.getApplicationId());
            return true;
        }

        if (sub.equals("issue")) {
            if (args.length < 6) {
                player.sendMessage(ChatColor.YELLOW + "Usage: /visa issue <player> <town|nation> <authorityName> <age> <sex> [notes]");
                return true;
            }
            PassportRecord.AuthorityType type = parseAuthority(args[2]);
            int age = parseAge(args[4]);
            if (type == null || age < 0) {
                player.sendMessage(ChatColor.RED + "Invalid authority or age.");
                return true;
            }
            if (!canIssue(player, type, args[3])) {
                player.sendMessage(ChatColor.RED + "You cannot issue for this authority.");
                return true;
            }
            String notes = args.length > 6 ? String.join(" ", Arrays.copyOfRange(args, 6, args.length)) : "None";
            var target = Bukkit.getOfflinePlayer(args[1]);
            var record = service.issueDocument(PassportRecord.DocumentType.VISA, target, target.getName() == null ? args[1] : target.getName(), age, args[5], notes, type, args[3], player);
            if (record == null) {
                player.sendMessage(ChatColor.RED + "Could not issue visa.");
                return true;
            }
            player.sendMessage(ChatColor.GREEN + "Issued visa " + record.getDocumentId());
            return true;
        }

        if (sub.equals("approve") && args.length >= 2) {
            var app = service.getApplication(args[1]);
            if (app == null || app.getDocumentType() != PassportRecord.DocumentType.VISA) {
                player.sendMessage(ChatColor.RED + "Visa application not found.");
                return true;
            }
            if (!canIssue(player, app.getAuthorityType(), app.getAuthorityName())) {
                player.sendMessage(ChatColor.RED + "You cannot approve for this authority.");
                return true;
            }
            var record = service.approveApplication(args[1], player);
            if (record == null) {
                player.sendMessage(ChatColor.RED + "Could not approve visa application.");
                return true;
            }
            player.sendMessage(ChatColor.GREEN + "Approved visa application: " + record.getDocumentId());
            return true;
        }

        player.sendMessage(ChatColor.YELLOW + "Usage: /visa <apply|issue|approve> ...");
        return true;
    }

    private PassportRecord.AuthorityType parseAuthority(String input) {
        if (input.equalsIgnoreCase("town")) return PassportRecord.AuthorityType.TOWN;
        if (input.equalsIgnoreCase("nation")) return PassportRecord.AuthorityType.NATION;
        return null;
    }

    private int parseAge(String input) {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    private boolean canIssue(Player player, PassportRecord.AuthorityType type, String authority) {
        return type == PassportRecord.AuthorityType.TOWN
                ? townyHook.canIssueTownDocuments(player, authority)
                : townyHook.canIssueNationDocuments(player, authority);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        String sub = args.length > 0 ? args[0].toLowerCase(Locale.ROOT) : "";

        if (args.length == 1) {
            return completePrefix(args[0], List.of("apply", "issue", "approve"));
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

        if (sub.equals("approve") && args.length == 2) {
            return completePrefix(args[1], service.getVisaApplicationIds());
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
