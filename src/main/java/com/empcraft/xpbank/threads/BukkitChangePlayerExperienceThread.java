package com.empcraft.xpbank.threads;

import com.empcraft.xpbank.ExpBankConfig;
import com.empcraft.xpbank.events.PlayerExperienceChangeEvent;
import com.empcraft.xpbank.logic.ExperienceLevelCalculator;
import com.empcraft.xpbank.text.YamlLanguageProvider;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.concurrent.atomic.AtomicInteger;

public class BukkitChangePlayerExperienceThread implements Runnable {

  private Player player;

  private ExpBankConfig config;
  private YamlLanguageProvider language;

  private int delta;

  private AtomicInteger currentXp;

  public BukkitChangePlayerExperienceThread(Player player, AtomicInteger currentExp, int delta,
      ExpBankConfig config,
      YamlLanguageProvider language) {
    this.player = player;
    this.config = config;
    this.language = language;
    this.delta = delta;
    this.currentXp = currentExp;
  }

  @Override
  public void run() {
    PlayerExperienceChangeEvent pece = new PlayerExperienceChangeEvent(player.getUniqueId(), delta,
        config, language);
    Bukkit.getServer().getPluginManager().callEvent(pece);

    int newExperienceInBank = currentXp.addAndGet(delta);
    config.getLogger().info("Player new experience in bank: " + newExperienceInBank);

    /* set total exp */
    player.setTotalExperience(player.getTotalExperience() - delta);
    config.getLogger().info("Player new experience on Hand: " + player.getTotalExperience());

    /* set level */
    int level = ExperienceLevelCalculator.getLevel(player.getTotalExperience());
    config.getLogger().info("Player new level: " + level);
    player.setLevel(level);

    /* Set experience bar */
    int neededForNextLevel = ExperienceLevelCalculator.getMinExperienceForLevel(level + 1);
    int neededForCurrentLevel = ExperienceLevelCalculator.getMinExperienceForLevel(level);
    float percentage = (player.getTotalExperience() - neededForCurrentLevel) / neededForNextLevel;
    if (percentage >= 1.0f) {
      percentage = 1.0f;
    }
    if (percentage <= 0.0f) {
      percentage = 0.0f;
    }

    player.setExp(percentage);

  }


}
