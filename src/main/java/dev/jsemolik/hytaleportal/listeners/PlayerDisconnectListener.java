package dev.jsemolik.hytaleportal.listeners;

import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import dev.jsemolik.hytaleportal.HytalePortal;
import dev.jsemolik.hytaleportal.portal.PortalManager;

import java.util.UUID;

/**
 * Listens for player disconnections to clean up their portals.
 */
public class PlayerDisconnectListener {
    
    /**
     * Register this listener with the event registry
     */
    public static void register(HytalePortal plugin) {
        plugin.getEventRegistry().register(PlayerDisconnectEvent.class, event -> {
            handleDisconnect(event);
        });
    }
    
    private static void handleDisconnect(PlayerDisconnectEvent event) {
        UUID playerUUID = event.getPlayerRef().getUuid();
        String playerName = event.getPlayerRef().getUsername();
        
        // Remove all portals for this player
        PortalManager.getInstance().removeAllPortals(playerUUID);
        
        HytalePortal.getPluginLogger().atInfo().log(
            "Removed portals for disconnected player: {}",
            playerName
        );
    }
}
