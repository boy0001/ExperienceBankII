/**
 *
 */
package com.empcraft.xpbank;

import com.empcraft.xpbank.events.PlayerExperienceChangeEvent;
import com.empcraft.xpbank.logic.ExperienceLevelCalculator;
import com.empcraft.xpbank.text.YamlLanguageProvider;

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

    PlayerExperienceChangeEvent pece = new PlayerExperienceChangeEvent(player.getUniqueId(), delta,
        config,
        language);
    Bukkit.getServer().getPluginManager().callEvent(pece);

    int newExperienceInBank = this.get(player.getUniqueId()).addAndGet(delta);
    config.getLogger().info("Player new experience in bank: " + newExperienceInBank);

    player.setTotalExperience(player.getTotalExperience() - delta);
    config.getLogger().info("Player new experience on Hand: " + player.getTotalExperience());

    int level = ExperienceLevelCalculator.getLevel(player.getTotalExperience());
    config.getLogger().info("Player new level: " + level);
    player.setLevel(level);
  }

  public void substractExperience(Player player, int withdrawAmount, final ExpBankConfig config,
      final YamlLanguageProvider ylp) {
    addExperience(player, withdrawAmount * -1, config, ylp);
  }
}