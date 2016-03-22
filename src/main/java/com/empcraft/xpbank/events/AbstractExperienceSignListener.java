package com.empcraft.xpbank.events;

import com.empcraft.xpbank.ExpBankConfig;
import com.empcraft.xpbank.logic.SignHelper;
import com.empcraft.xpbank.text.YamlLanguageProvider;
import com.google.common.base.Optional;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public abstract class AbstractExperienceSignListener implements Listener {

  private final YamlLanguageProvider ylp;
  private final ExpBankConfig config;

  public AbstractExperienceSignListener(final YamlLanguageProvider ylp,
      final ExpBankConfig config) {
    this.ylp = ylp;
    this.config = config;
  }

  protected ExpBankConfig getConfig() {
    return config;
  }

  protected YamlLanguageProvider getYlp() {
    return ylp;
  }

  protected static boolean isSignForEvent(ExpBankConfig config, PlayerInteractEvent event,
      Action desiredAction,
      Optional<Boolean> bottleRequiredInHand, Optional<Boolean> sneekingRequired) {

    if (!desiredAction.equals(event.getAction())) {
      return false;
    }

    if (!SignHelper.isExperienceBankSignBlock(event.getClickedBlock(), config)) {
      return false;
    }

    Player player = event.getPlayer();
    if (!isBottleRequirementSatisfied(bottleRequiredInHand, player)) {
      return false;
    }

    if (sneekingRequired.isPresent()
        && sneekingRequired.get().booleanValue() != player.isSneaking()) {
      return false;
    }

    return true;
  }

  private static boolean isBottleRequirementSatisfied(Optional<Boolean> bottleRequiredInHand,
      Player player) {
    if (bottleRequiredInHand.isPresent() && bottleRequiredInHand.get().booleanValue()
        && !(player.getInventory().getItemInMainHand().getType() == Material.GLASS_BOTTLE)) {
      // if we require to have a glass in hand and it is not…
      return false;
    }

    if (bottleRequiredInHand.isPresent() && !bottleRequiredInHand.get().booleanValue()
        && (player.getInventory().getItemInMainHand().getType() == Material.GLASS_BOTTLE)) {
      // We require him not to have a glass in hand.
      return false;
    }

    return true;
  }

}
