package com.empcraft.xpbank.listeners;

import com.empcraft.xpbank.ExpBankConfig;
import com.empcraft.xpbank.logic.SignHelper;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveListener implements Listener {

  private ExpBankConfig config;

  public PlayerMoveListener(final ExpBankConfig config) {
    this.config = config;
  }

  @EventHandler
  public void onPlayerMove(PlayerMoveEvent event) {
    if (event.getFrom().getChunk().equals(event.getTo().getChunk())) {
      // player moved within the same chunk. No update needed.
      return;
    }

    SignHelper.scheduleUpdate(event.getPlayer(), event.getTo(), config);
  }

}
