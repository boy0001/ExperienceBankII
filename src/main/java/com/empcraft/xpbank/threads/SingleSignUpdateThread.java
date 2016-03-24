package com.empcraft.xpbank.threads;

import com.empcraft.xpbank.ExpBankConfig;
import com.empcraft.xpbank.logic.SignHelper;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

public class SingleSignUpdateThread implements Runnable {

  private Player player;
  private Sign sign;
  private Location location;
  private final ExpBankConfig config;

  public SingleSignUpdateThread(final Player player, final Sign sign, final ExpBankConfig config) {
    this.player = player;
    this.sign = sign;
    this.location = sign.getLocation();
    this.config = config;
  }

  @Override
  public void run() {
    Block block = location.getBlock();

    if (block == null) {
      return;
    }

    if (block.getState() == null) {
      return;
    }

    if (!(block.getState() instanceof Sign)) {
      return;
    }

    if (player == null || !player.isOnline()) {
      return;
    }

    if (!location.getWorld().equals(player.getWorld())) {
      return;
    }

    if (!location.getChunk().isLoaded()) {
      return;
    }

    double distance = location.distanceSquared(player.getLocation());

    if (distance > 1024) {
      return;
    }

    String[] lines = SignHelper.getSignText(sign.getLines(), player, config);

    if (lines == null) {
      return;
    }

    String[] signText = SignHelper.getSignText(lines, player,
        config);

    player.sendSignChange(sign.getLocation(), signText);
    sign.update();
  }

}
