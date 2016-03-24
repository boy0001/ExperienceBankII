package com.empcraft.xpbank.listeners;

import com.empcraft.xpbank.events.PlayerExperienceChangeEvent;
import com.empcraft.xpbank.threads.ChangeExperienceThread;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerExperienceChangedListener implements Listener {

  @EventHandler
  public void onPlayerExperienceChanged(PlayerExperienceChangeEvent event) {
    ChangeExperienceThread cet = new ChangeExperienceThread(event.getPlayerUuid(), event.getDelta(),
        event.getConfig(), event.getTranslation());
    Bukkit.getScheduler().runTaskAsynchronously(event.getConfig().getPlugin(), cet);
  }

}
