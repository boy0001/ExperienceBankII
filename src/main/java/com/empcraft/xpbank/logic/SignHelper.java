package com.empcraft.xpbank.logic;

import com.empcraft.xpbank.ExpBank;
import com.empcraft.xpbank.ExpBankConfig;
import com.empcraft.xpbank.text.MessageUtils;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

public final class SignHelper {

  public static boolean updateSign(Player player, Sign sign, final ExpBankConfig expBankConfig) {
    Location location = sign.getLocation();
    Block block = location.getBlock();

    if (block == null) {
      return false;
    }

    if (block.getState() == null) {
      return false;
    }

    if (!(block.getState() instanceof Sign)) {
      return false;
    }

    if (player == null || !player.isOnline()) {
      return false;
    }

    if (!location.getWorld().equals(player.getWorld())) {
      return false;
    }

    if (!location.getChunk().isLoaded()) {
      return false;
    }

    double distance = location.distanceSquared(player.getLocation());

    if (distance > 1024) {
      return false;
    }

    String[] lines = MessageUtils.getSignText(sign.getLines(), player, sign, expBankConfig);

    if (lines == null) {
      return false;
    }

    for (int i = 0; i < 4; i++) {
      if (lines[i].contains("\n")) {
        if ((i < 3)) {
          if (lines[i + 1].isEmpty()) {
            lines[i + 1] = ChatColor.getLastColors(lines[i].substring(0, 15))
                + lines[i].substring(lines[i].indexOf("\n") + 1);
          }
        }

        lines[i] = lines[i].substring(0, lines[i].indexOf("\n"));
      }

      if (lines[i].length() > 15) {
        if ((i < 3)) {
          if (lines[i + 1].isEmpty()) {
            lines[i + 1] = ChatColor.getLastColors(lines[i].substring(0, 15))
                + lines[i].substring(15);
          }
        }

        lines[i] = lines[i].substring(0, 15);
      }
    }

    player.sendSignChange(sign.getLocation(), lines);

    return true;
  }

}
