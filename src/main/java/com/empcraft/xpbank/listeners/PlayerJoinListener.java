package com.empcraft.xpbank.listeners;

import com.empcraft.xpbank.ExpBankConfig;
import com.empcraft.xpbank.logic.SignHelper;
import com.empcraft.xpbank.text.YamlLanguageProvider;
import com.empcraft.xpbank.threads.InsertPlayerThread;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

  private ExpBankConfig config;
  private YamlLanguageProvider ylp;

  public PlayerJoinListener(final ExpBankConfig config, final YamlLanguageProvider ylp) {
    this.config = config;
    this.ylp = ylp;
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    // insert player if not exists.
    InsertPlayerThread ipt = new InsertPlayerThread(event.getPlayer(), config, ylp);
    Bukkit.getServer().getScheduler().runTaskAsynchronously(config.getPlugin(), ipt);

    // update signs around player.
    SignHelper.scheduleUpdate(event.getPlayer(), event.getPlayer().getLocation(), config);

    config.getExperienceCache().addPlayer(event.getPlayer().getUniqueId());
  }

}
