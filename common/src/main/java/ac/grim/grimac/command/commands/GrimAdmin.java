package ac.grim.grimac.command.commands;

import ac.grim.grimac.command.BuildableCommand;
import ac.grim.grimac.platform.api.manager.cloud.CloudPlatformCommandArguments;
import ac.grim.grimac.platform.api.sender.Sender;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;

public class GrimAdmin implements BuildableCommand {

    private static final String[] ADMIN_TOOL_NODES = {
            "grim.alerts",
            "grim.verbose",
            "grim.profile",
            "grim.spectate",
            "grim.log",
            "grim.history",
            "grim.reload",
            "grim.dump"
    };

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
                        .literal("tools")
                        .permission("grim.admin.tools")
                        .handler(this::handleAdminOverview)
        );
        commandManager.command(
                commandManager.commandBuilder("grim", "grimac")
                        .literal("admin")
                        .literal("permissions")
                        .permission("grim.admin.permissions")
                        .handler(this::handlePermissionOverview)
        );
    }

    private void handleAdminOverview(CommandContext<Sender> context) {
        Sender sender = context.sender();
        sender.sendMessage(Component.text("Grim admin tools:", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("- /grim alerts, /grim verbose, /grim profile", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("- /grim spectate, /grim history, /grim log", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("- /grim reload, /grim dump", NamedTextColor.GRAY));
        sender.sendMessage(Component.text("Tip: /grim admin permissions", NamedTextColor.DARK_GRAY));
    }

    private void handlePermissionOverview(CommandContext<Sender> context) {
        Sender sender = context.sender();
        sender.sendMessage(Component.text("Grim admin permission status:", NamedTextColor.GOLD));
        for (String node : ADMIN_TOOL_NODES) {
            boolean allowed = sender.hasPermission(node);
            sender.sendMessage(Component.text("- " + node + ": ", NamedTextColor.GRAY)
                    .append(Component.text(allowed ? "granted" : "missing", allowed ? NamedTextColor.GREEN : NamedTextColor.RED)));
        }
    }
}
