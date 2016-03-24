package com.empcraft.xpbank.events;

import com.empcraft.xpbank.ExpBankConfig;
import com.empcraft.xpbank.text.YamlLanguageProvider;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.UUID;

public class PlayerExperienceChangeEvent extends Event {
  private static final HandlerList handlers = new HandlerList();

  private final int delta;
  private final UUID playerUuid;

  private final ExpBankConfig config;

  private final YamlLanguageProvider translation;

  public PlayerExperienceChangeEvent(final UUID playerUuid, final int delta,
      final ExpBankConfig config, final YamlLanguageProvider translation) {
    this.playerUuid = playerUuid;
    this.delta = delta;
    this.config = config;
    this.translation = translation;

    config.getLogger().info("Fired PlayerExperienceChangeEvent!");
  }

  @Override
  public HandlerList getHandlers() {
    return handlers;
  }

  public static HandlerList getHandlerList() {
    return handlers;
  }

  public int getDelta() {
    return delta;
  }

  public UUID getPlayerUuid() {
    return playerUuid;
  }

  public ExpBankConfig getConfig() {
    return config;
  }

  public YamlLanguageProvider getTranslation() {
    return translation;
  }

}
