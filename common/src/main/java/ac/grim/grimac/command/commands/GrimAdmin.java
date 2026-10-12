package ac.grim.grimac.command.commands;

import ac.grim.grimac.GrimAPI;
import ac.grim.grimac.api.AbstractCheck;
import ac.grim.grimac.checks.Check;
import ac.grim.grimac.command.BuildableCommand;
import ac.grim.grimac.command.CommandUtils;
import ac.grim.grimac.manager.AlertManagerImpl;
import ac.grim.grimac.manager.datastore.PlayerToggleStore;
import ac.grim.grimac.platform.api.command.PlayerSelector;
import ac.grim.grimac.platform.api.manager.cloud.CloudPlatformCommandArguments;
import ac.grim.grimac.platform.api.player.PlatformPlayer;
import ac.grim.grimac.platform.api.sender.Sender;
import ac.grim.grimac.player.GrimPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.parser.standard.StringParser;
import org.incendo.cloud.suggestion.SuggestionProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Locale;
import java.util.Objects;

public class GrimAdmin implements BuildableCommand {

    private static final SuggestionProvider<Sender> TOGGLE_STATE_SUGGESTIONS = CommandUtils.fromStrings("on", "off", "toggle");

    @Override
    public void register(CommandManager<Sender> commandManager, CloudPlatformCommandArguments arguments) {
        commandManager.command(
                commandManager.commandBuilder("grim", "grimac")
                        .literal("admin")
                        .permission("grim.admin")
                        .handler(this::handleAdminOverview)
        );
        commandManager.command(
                commandManager.commandBuilder("grim", "grimac")
                        .literal("admin")
                        .literal("playerstats")
                        .permission("grim.admin.playerstats")
                        .required("target", arguments.singlePlayerSelectorParser())
                        .handler(this::handlePlayerStats)
        );
        commandManager.command(
                commandManager.commandBuilder("grim", "grimac")
                        .literal("admin")
                        .literal("staffstats")
                        .permission("grim.admin.supervisor")
                        .handler(this::handleStaffStats)
        );
        commandManager.command(
                commandManager.commandBuilder("grim", "grimac")
                        .literal("admin")
                        .literal("refreshplayer")
                        .permission("grim.admin.player.refresh")
                        .required("target", arguments.singlePlayerSelectorParser())
                        .handler(this::handleRefreshPlayer)
        );
        commandManager.command(
                commandManager.commandBuilder("grim", "grimac")
                        .literal("admin")
                        .literal("setalerts")
                        .permission("grim.admin.alerts.manage")
                        .required("target", arguments.singlePlayerSelectorParser())
                        .required("state", StringParser.stringParser(), TOGGLE_STATE_SUGGESTIONS)
                        .handler(this::handleSetAlerts)
        );
        commandManager.command(
                commandManager.commandBuilder("grim", "grimac")
                        .literal("admin")
                        .literal("setverbose")
                        .permission("grim.admin.verbose.manage")
                        .required("target", arguments.singlePlayerSelectorParser())
                        .required("state", StringParser.stringParser(), TOGGLE_STATE_SUGGESTIONS)
                        .handler(this::handleSetVerbose)
        );
        commandManager.command(
                commandManager.commandBuilder("grim", "grimac")
                        .literal("admin")
                        .literal("setbrands")
                        .permission("grim.admin.brands.manage")
                        .required("target", arguments.singlePlayerSelectorParser())
                        .required("state", StringParser.stringParser(), TOGGLE_STATE_SUGGESTIONS)
                        .handler(this::handleSetBrands)
        );
        commandManager.command(
                commandManager.commandBuilder("grim", "grimac")
                        .literal("admin")
                        .literal("tools")
                        .permission("grim.admin")
                        .handler(this::handleAdminOverview)
        );
        commandManager.command(
                commandManager.commandBuilder("grim", "grimac")
                        .literal("admin")
                        .literal("permissions")
                        .permission("grim.admin.supervisor")
                        .handler(this::handlePermissionOverview)
        );
    }

    private void handleAdminOverview(CommandContext<Sender> context) {
        Sender sender = context.sender();
        sender.sendMessage(Component.text("Grim admin command center:", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("/grim admin playerstats <player>", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("/grim admin staffstats", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("/grim admin refreshplayer <player>", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("/grim admin setalerts <player> <on|off|toggle>", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("/grim admin setverbose <player> <on|off|toggle>", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("/grim admin setbrands <player> <on|off|toggle>", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("Supervisor: /grim admin permissions", NamedTextColor.DARK_GRAY));
    }

    private void handlePlayerStats(CommandContext<Sender> context) {
        Sender sender = context.sender();
        PlatformPlayer target = resolveTargetPlatformPlayer(sender, context.get("target"));
        if (target == null) return;

        GrimPlayer grimPlayer = GrimAPI.INSTANCE.getPlayerDataManager().getPlayer(target.getUniqueId());
        if (grimPlayer == null) {
            sender.sendMessage(Component.text("Player data is unavailable (offline/exempt).", NamedTextColor.RED));
            return;
        }

        int totalChecks = 0;
        int enabledChecks = 0;
        int flaggedChecks = 0;
        double totalViolations = 0.0D;
        for (AbstractCheck abstractCheck : grimPlayer.getChecks()) {
            if (!(abstractCheck instanceof Check check)) continue;
            totalChecks++;
            if (check.isEnabled()) enabledChecks++;
            if (check.violations > 0.0D) {
                flaggedChecks++;
                totalViolations += check.violations;
            }
        }

        AlertManagerImpl alertManager = GrimAPI.INSTANCE.getAlertManager();
        sender.sendMessage(Component.text("Player stats: " + target.getName(), NamedTextColor.GOLD));
        sender.sendMessage(Component.text("- UUID: " + target.getUniqueId(), NamedTextColor.GRAY));
        sender.sendMessage(Component.text("- World: " + target.getWorld().getName(), NamedTextColor.GRAY));
        sender.sendMessage(Component.text("- Version: " + grimPlayer.getVersionName(), NamedTextColor.GRAY));
        sender.sendMessage(Component.text("- Brand: " + nullToUnknown(grimPlayer.getBrand()), NamedTextColor.GRAY));
        sender.sendMessage(Component.text("- KeepAlive ping: " + grimPlayer.getKeepAlivePing() + "ms", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("- Transaction ping: " + grimPlayer.getTransactionPing() + "ms", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("- Check state: " + enabledChecks + "/" + totalChecks + " enabled", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("- Violations: " + flaggedChecks + " checks, total VL " + Math.round(totalViolations), NamedTextColor.GRAY));
        sender.sendMessage(Component.text("- Runtime flags: alerts=" + onOff(alertManager.hasAlertsEnabled(target))
                + ", verbose=" + onOff(alertManager.hasVerboseEnabled(target))
                + ", brands=" + onOff(alertManager.hasBrandsEnabled(target))
                + ", spectating=" + onOff(GrimAPI.INSTANCE.getSpectateManager().isSpectating(target.getUniqueId())), NamedTextColor.GRAY));
    }

    private void handleStaffStats(CommandContext<Sender> context) {
        Sender sender = context.sender();
        AlertManagerImpl alertManager = GrimAPI.INSTANCE.getAlertManager();
        Collection<PlatformPlayer> onlinePlayers = GrimAPI.INSTANCE.getPlatformPlayerFactory().getOnlinePlayers();

        int totalOnline = onlinePlayers.size();
        int adminOnline = 0;
        int supervisorOnline = 0;
        int alertsOn = 0;
        int verboseOn = 0;
        int brandsOn = 0;
        int spectating = 0;

        for (PlatformPlayer online : onlinePlayers) {
            if (!online.hasPermission("grim.admin")) continue;
            adminOnline++;
            if (online.hasPermission("grim.admin.supervisor")) supervisorOnline++;
            if (alertManager.hasAlertsEnabled(online)) alertsOn++;
            if (alertManager.hasVerboseEnabled(online)) verboseOn++;
            if (alertManager.hasBrandsEnabled(online)) brandsOn++;
            if (GrimAPI.INSTANCE.getSpectateManager().isSpectating(online.getUniqueId())) spectating++;
        }

        sender.sendMessage(Component.text("Staff admin stats", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("- Online players: " + totalOnline, NamedTextColor.GRAY));
        sender.sendMessage(Component.text("- Online admins (grim.admin): " + adminOnline, NamedTextColor.GRAY));
        sender.sendMessage(Component.text("- Online supervisors (grim.admin.supervisor): " + supervisorOnline, NamedTextColor.GRAY));
        if (adminOnline == 0) {
            sender.sendMessage(Component.text("- No admins are online right now.", NamedTextColor.RED));
            return;
        }
        sender.sendMessage(Component.text("- Admin runtime states: alerts " + alertsOn + "/" + adminOnline
                + ", verbose " + verboseOn + "/" + adminOnline
                + ", brands " + brandsOn + "/" + adminOnline
                + ", spectating " + spectating + "/" + adminOnline, NamedTextColor.GRAY));
    }

    private void handleRefreshPlayer(CommandContext<Sender> context) {
        Sender sender = context.sender();
        PlatformPlayer target = resolveTargetPlatformPlayer(sender, context.get("target"));
        if (target == null) return;

        GrimPlayer grimPlayer = GrimAPI.INSTANCE.getPlayerDataManager().getPlayer(target.getUniqueId());
        if (grimPlayer == null) {
            sender.sendMessage(Component.text("Player data is unavailable (offline/exempt).", NamedTextColor.RED));
            return;
        }

        grimPlayer.reload();
        sender.sendMessage(Component.text("Refreshed Grim runtime state for " + target.getName() + ".", NamedTextColor.GREEN));
    }

    private void handleSetAlerts(CommandContext<Sender> context) {
        handleSetToggle(context, PlayerToggleStore.KEY_ALERTS, "alerts");
    }

    private void handleSetVerbose(CommandContext<Sender> context) {
        handleSetToggle(context, PlayerToggleStore.KEY_VERBOSE, "verbose");
    }

    private void handleSetBrands(CommandContext<Sender> context) {
        handleSetToggle(context, PlayerToggleStore.KEY_BRANDS, "brands");
    }

    private void handleSetToggle(CommandContext<Sender> context, String key, String label) {
        Sender sender = context.sender();
        PlatformPlayer target = resolveTargetPlatformPlayer(sender, context.get("target"));
        if (target == null) return;

        ToggleAction action = ToggleAction.parse(context.get("state"));
        if (action == null) {
            sender.sendMessage(Component.text("Invalid state. Use: on, off, toggle.", NamedTextColor.RED));
            return;
        }

        AlertManagerImpl alertManager = GrimAPI.INSTANCE.getAlertManager();
        boolean current = switch (key) {
            case PlayerToggleStore.KEY_ALERTS -> alertManager.hasAlertsEnabled(target);
            case PlayerToggleStore.KEY_VERBOSE -> alertManager.hasVerboseEnabled(target);
            case PlayerToggleStore.KEY_BRANDS -> alertManager.hasBrandsEnabled(target);
            default -> false;
        };
        boolean next = switch (action) {
            case ON -> true;
            case OFF -> false;
            case TOGGLE -> !current;
        };

        switch (key) {
            case PlayerToggleStore.KEY_ALERTS -> {
                alertManager.setAlertsEnabled(target, next, false);
                GrimAPI.INSTANCE.getDataStoreLifecycle().playerToggleStore()
                        .applyUserToggle(target.getUniqueId(), PlayerToggleStore.KEY_ALERTS, next);
                if (!next) {
                    GrimAPI.INSTANCE.getDataStoreLifecycle().playerToggleStore()
                            .applyUserToggle(target.getUniqueId(), PlayerToggleStore.KEY_VERBOSE, false);
                }
            }
            case PlayerToggleStore.KEY_VERBOSE -> {
                alertManager.setVerboseEnabled(target, next, false);
                GrimAPI.INSTANCE.getDataStoreLifecycle().playerToggleStore()
                        .applyUserToggle(target.getUniqueId(), PlayerToggleStore.KEY_VERBOSE, next);
                if (next) {
                    GrimAPI.INSTANCE.getDataStoreLifecycle().playerToggleStore()
                            .applyUserToggle(target.getUniqueId(), PlayerToggleStore.KEY_ALERTS, true);
                }
            }
            case PlayerToggleStore.KEY_BRANDS -> {
                alertManager.setBrandsEnabled(target, next, false);
                GrimAPI.INSTANCE.getDataStoreLifecycle().playerToggleStore()
                        .applyUserToggle(target.getUniqueId(), PlayerToggleStore.KEY_BRANDS, next);
            }
            default -> throw new IllegalStateException("Unsupported toggle key: " + key);
        }

        sender.sendMessage(Component.text("Set " + label + " for " + target.getName() + " to " + onOff(next) + ".", NamedTextColor.GREEN));
    }

    private void handlePermissionOverview(CommandContext<Sender> context) {
        Sender sender = context.sender();
        String[] nodes = {
                "grim.admin",
                "grim.admin.supervisor",
                "grim.admin.playerstats",
                "grim.admin.player.refresh",
                "grim.admin.alerts.manage",
                "grim.admin.verbose.manage",
                "grim.admin.brands.manage",
                "grim.alerts",
                "grim.verbose",
                "grim.spectate",
                "grim.history"
        };

        sender.sendMessage(Component.text("Grim admin permission status:", NamedTextColor.GOLD));
        for (String node : nodes) {
            boolean allowed = sender.hasPermission(node);
            sender.sendMessage(Component.text("- " + node + ": ", NamedTextColor.GRAY)
                    .append(Component.text(allowed ? "granted" : "missing", allowed ? NamedTextColor.GREEN : NamedTextColor.RED)));
        }
    }

    private @Nullable PlatformPlayer resolveTargetPlatformPlayer(@NotNull Sender sender, @NotNull PlayerSelector selector) {
        Sender targetSender = selector.getSinglePlayer();
        PlatformPlayer target = Objects.requireNonNull(targetSender, "targetSender").getPlatformPlayer();
        if (target == null || target.isExternalPlayer()) {
            sender.sendMessage(Component.text("Target must be an online local player.", NamedTextColor.RED));
            return null;
        }
        return target;
    }

    private static String onOff(boolean value) {
        return value ? "on" : "off";
    }

    private static String nullToUnknown(@Nullable String value) {
        return value == null || value.isBlank() ? "unknown" : value;
    }

    private enum ToggleAction {
        ON,
        OFF,
        TOGGLE;

        private static @Nullable ToggleAction parse(String input) {
            String normalized = input == null ? "" : input.toLowerCase(Locale.ROOT);
            return switch (normalized) {
                case "on" -> ON;
                case "off" -> OFF;
                case "toggle" -> TOGGLE;
                default -> null;
            };
        }
    }
}
