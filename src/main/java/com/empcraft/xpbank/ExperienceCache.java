/**
 *
 */
package com.empcraft.xpbank;

import com.empcraft.xpbank.events.PlayerExperienceChangeEvent;
import com.empcraft.xpbank.text.YamlLanguageProvider;

import org.bukkit.Bukkit;

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

  public void addExperience(UUID playerUuid, int delta, final ExpBankConfig config,
      final YamlLanguageProvider language) {
    if (this.containsKey(playerUuid)) {
      addPlayer(playerUuid);
    }

    PlayerExperienceChangeEvent pece = new PlayerExperienceChangeEvent(playerUuid, delta, config,
        language);
    Bukkit.getServer().getPluginManager().callEvent(pece);

    this.get(playerUuid).addAndGet(delta);
  }

  public void substractExperience(UUID playerUuid, int withdrawAmount, final ExpBankConfig config,
      final YamlLanguageProvider ylp) {
    addExperience(playerUuid, withdrawAmount * -1, config, ylp);
  }
}
