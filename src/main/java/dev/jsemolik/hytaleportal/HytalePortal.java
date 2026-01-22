package dev.jsemolik.hytaleportal;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import dev.jsemolik.hytaleportal.commands.PortalGunCommand;
import dev.jsemolik.hytaleportal.listeners.PlayerDisconnectListener;
import dev.jsemolik.hytaleportal.listeners.PortalGunListener;
import dev.jsemolik.hytaleportal.listeners.PortalTeleportListener;
import dev.jsemolik.hytaleportal.portal.PortalManager;
import dev.jsemolik.hytaleportal.portal.PortalVisualizer;

public class HytalePortal extends JavaPlugin {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static HytalePortal instance;
    private PortalVisualizer portalVisualizer;

    public HytalePortal(JavaPluginInit init) {
        super(init);
        instance = this;
        LOGGER.atInfo().log("Hello from {} version {}", this.getName(), this.getManifest().getVersion().toString());
    }

    @Override
    protected void setup() {
        LOGGER.atInfo().log("Setting up HytalePortal plugin...");

        // Register commands
        this.getCommandRegistry().registerCommand(new PortalGunCommand());
        this.getCommandRegistry().registerCommand(new ExampleCommand(this.getName(), this.getManifest().getVersion().toString()));

        // Register event listeners
        PortalGunListener.register(this);
        PlayerDisconnectListener.register(this);

        LOGGER.atInfo().log("HytalePortal plugin setup complete!");
    }

    @Override
    protected void start() {
        LOGGER.atInfo().log("Starting HytalePortal plugin...");

        // Start the portal visualizer
        portalVisualizer = new PortalVisualizer();
        portalVisualizer.start();

        // Start the portal teleport checker
        PortalTeleportListener.start();

        LOGGER.atInfo().log("HytalePortal plugin started!");
    }

    @Override
    protected void shutdown() {
        LOGGER.atInfo().log("Shutting down HytalePortal plugin...");

        // Stop the visualizer
        if (portalVisualizer != null) {
            portalVisualizer.stop();
        }

        // Stop the teleport checker
        PortalTeleportListener.stop();

        // Clear all portals on shutdown
        PortalManager.getInstance().clearAll();

        LOGGER.atInfo().log("HytalePortal plugin shutdown complete!");
    }

    public static HytalePortal getInstance() {
        return instance;
    }

    public static HytaleLogger getPluginLogger() {
        return LOGGER;
    }

    public PortalVisualizer getPortalVisualizer() {
        return portalVisualizer;
    }
}
