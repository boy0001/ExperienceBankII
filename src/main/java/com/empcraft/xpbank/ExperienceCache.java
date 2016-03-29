/**
 *
 */
package com.empcraft.xpbank;

import com.empcraft.xpbank.text.YamlLanguageProvider;
import com.empcraft.xpbank.threads.BukkitChangePlayerExperienceThread;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages Experience Cache.
 */
public class ExperienceCache extends HashMap<UUID, AtomicInteger> {
  /**
   * Serial.
   */
  private static final long serialVersionUID = -8729259291859204345L;

  public AtomicInteger addPlayer(UUID uuid) {
    if (this.containsKey(uuid)) {
      return this.get(uuid);
    }

    return this.put(uuid, new AtomicInteger());
  }

  public void addExperience(Player player, int delta, final ExpBankConfig config,
      final YamlLanguageProvider language) {
    if (this.containsKey(player.getUniqueId())) {
      addPlayer(player.getUniqueId());
    }

    AtomicInteger playerInCache = this.get(player.getUniqueId());

    playerInCache.addAndGet(delta);
    config.getLogger().info("Player new experience in bank: " + playerInCache.get());

    BukkitChangePlayerExperienceThread experienceThread = new BukkitChangePlayerExperienceThread(
        player, delta, config, language);
    Bukkit.getScheduler().runTask(config.getPlugin(), experienceThread);
  }

  public void substractExperience(Player player, int withdrawAmount, final ExpBankConfig config,
      final YamlLanguageProvider ylp) {
    addExperience(player, withdrawAmount * -1, config, ylp);
  }
}
