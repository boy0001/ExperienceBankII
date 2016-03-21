/**
 *
 */
package com.empcraft.xpbank.events;

import com.empcraft.xpbank.InSignsNano;
import com.empcraft.xpbank.text.MessageUtils;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

/**
 * Handles sign break events.
 */
public class SignBreakListener implements Listener {

  private InSignsNano signListener;
  private FileConfiguration config;

  public SignBreakListener(final InSignsNano signListener, final FileConfiguration config) {
    this.signListener = signListener;
    this.config = config;
  }

  @EventHandler
  public void onBlockBreak(BlockBreakEvent event) {
    Block block = event.getBlock();

    if ((block.getType() == Material.SIGN_POST) || (block.getType() == Material.WALL_SIGN)) {
      Sign sign = (Sign) block.getState();

      if (sign.getLine(0).equals(MessageUtils.colorise(config.getString("text.create")))) {
        signListener.broken_signs.add(event.getBlock().getLocation());
      }
    }
  }
}
