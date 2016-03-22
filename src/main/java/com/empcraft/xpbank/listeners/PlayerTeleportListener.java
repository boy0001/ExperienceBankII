package com.empcraft.xpbank.listeners;

import com.empcraft.xpbank.ExpBankConfig;
import com.empcraft.xpbank.logic.SignHelper;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

public class PlayerTeleportListener implements Listener {

  private ExpBankConfig config;

  public PlayerTeleportListener(final ExpBankConfig config) {
    this.config = config;
  }

  @EventHandler
  public void onPlayerTeleport(PlayerTeleportEvent event) {
    if (event.getFrom().getChunk().equals(event.getTo().getChunk())) {
      // Teleport to the same chunk - no update needed.
      return;
    }

    SignHelper.scheduleUpdate(event.getPlayer(), event.getPlayer().getLocation(), config);
  }

}
