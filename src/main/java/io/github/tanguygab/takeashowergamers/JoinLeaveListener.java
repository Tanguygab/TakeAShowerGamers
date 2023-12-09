package io.github.tanguygab.takeashowergamers;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinLeaveListener implements Listener {

    private final TakeAShowerGamers plugin;

    public JoinLeaveListener(TakeAShowerGamers plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        plugin.leftAt.put(e.getPlayer().getUniqueId(),plugin.now());
    }

}
