package ac.grim.grimac.platform.fabric.manager;

import ac.grim.grimac.platform.api.manager.PermissionRegistrationManager;
import ac.grim.grimac.platform.api.permissions.PermissionDefaultValue;
import ac.grim.grimac.platform.fabric.sender.AbstractFabricSenderFactory;

import java.util.function.Consumer;

/**
 * Registers Grim's permission defaults the same way for every Fabric mapping family.
 *
 * <p>Each call does two things:
 * <ul>
 *   <li>{@code fabricSenderFactory.registerPermissionDefault(...)} records Grim's default
 *       (e.g. {@code grim.exempt = FALSE}) in the shared registry, so an unset permission
 *       resolves to that default instead of silently granting access.</li>
 *   <li>{@code onRegister.accept(name)} runs the per-variant permissions-API hook. That hook is
 *       exactly the old {@code if (HAS_PERMISSIONS_API) Permissions.check(commandSource, name)}
 *       line -- it was not removed, only lifted out to the loader plugins, because it touches
 *       {@code net.minecraft} command sources and the {@code Permissions} API and so cannot live
 *       in the NMS-free fabric-common module. Each loader plugin passes that lambda in; a variant
 *       with nothing to prime passes a no-op.</li>
 * </ul>
 */
public class FabricPermissionRegistrationManager implements PermissionRegistrationManager {

    private final AbstractFabricSenderFactory<?> fabricSenderFactory;
    private final Consumer<String> onRegister;

    public FabricPermissionRegistrationManager(AbstractFabricSenderFactory<?> fabricSenderFactory,
                                               Consumer<String> onRegister) {
        this.fabricSenderFactory = fabricSenderFactory;
        this.onRegister = onRegister;
        registerPermission("grim.exempt", PermissionDefaultValue.FALSE);
        registerPermission("grim.nosetback", PermissionDefaultValue.FALSE);
        registerPermission("grim.nomodifypacket", PermissionDefaultValue.FALSE);
        registerPermission("grim.disabled", PermissionDefaultValue.FALSE);
        registerPermission("grim.alerts.enable-on-join", PermissionDefaultValue.FALSE);
        registerPermission("grim.verbose.enable-on-join", PermissionDefaultValue.FALSE);
        registerPermission("grim.brand.enable-on-join", PermissionDefaultValue.FALSE);
        registerPermission("grim.alerts.enable-on-join.silent", PermissionDefaultValue.FALSE);
        registerPermission("grim.verbose.enable-on-join.silent", PermissionDefaultValue.FALSE);
        registerPermission("grim.brand.enable-on-join.silent", PermissionDefaultValue.FALSE);
        registerPermission("grim.help", PermissionDefaultValue.OP);
        registerPermission("grim.performance", PermissionDefaultValue.OP);
        registerPermission("grim.profile", PermissionDefaultValue.OP);
        registerPermission("grim.brand", PermissionDefaultValue.OP);
        registerPermission("grim.sendalert", PermissionDefaultValue.OP);
        registerPermission("grim.verbose", PermissionDefaultValue.OP);
        registerPermission("grim.list", PermissionDefaultValue.FALSE);
        registerPermission("grim.reload", PermissionDefaultValue.OP);
        registerPermission("grim.version", PermissionDefaultValue.OP);
        registerPermission("grim.debug", PermissionDefaultValue.OP);
        registerPermission("grim.consoledebug", PermissionDefaultValue.OP);
        registerPermission("grim.spectate", PermissionDefaultValue.OP);
        registerPermission("grim.spectate.stophere", PermissionDefaultValue.OP);
        registerPermission("grim.history", PermissionDefaultValue.OP);
        registerPermission("grim.history.copy", PermissionDefaultValue.OP);
        registerPermission("grim.history.migrate", PermissionDefaultValue.OP);
        registerPermission("grim.history.repair", PermissionDefaultValue.OP);
        registerPermission("grim.log", PermissionDefaultValue.OP);
        registerPermission("grim.dump", PermissionDefaultValue.OP);
        registerPermission("grim.testwebhook", PermissionDefaultValue.OP);
        registerPermission("grim.admin", PermissionDefaultValue.OP);
        registerPermission("grim.admin.supervisor", PermissionDefaultValue.OP);
        registerPermission("grim.admin.playerstats", PermissionDefaultValue.OP);
        registerPermission("grim.admin.combatintel", PermissionDefaultValue.OP);
        registerPermission("grim.admin.player.refresh", PermissionDefaultValue.OP);
        registerPermission("grim.admin.alerts.manage", PermissionDefaultValue.OP);
        registerPermission("grim.admin.verbose.manage", PermissionDefaultValue.OP);
        registerPermission("grim.admin.brands.manage", PermissionDefaultValue.OP);
    }

    @Override
    public void registerPermission(String name, PermissionDefaultValue defaultValue) {
        fabricSenderFactory.registerPermissionDefault(name, defaultValue);
        onRegister.accept(name);
    }
}
