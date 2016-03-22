package com.empcraft.xpbank.listeners;

import com.empcraft.xpbank.ExpBankConfig;
import com.empcraft.xpbank.logic.SignHelper;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

  private ExpBankConfig config;

  public PlayerJoinListener(final ExpBankConfig config) {
    this.config = config;
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    SignHelper.scheduleUpdate(event.getPlayer(), event.getPlayer().getLocation(), config);
  }

}
