package com.empcraft.xpbank.listeners;

import com.empcraft.xpbank.ExpBankConfig;
import com.empcraft.xpbank.logic.SignHelper;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Updates existing signs.
 */
public class SignChangeUpdateSign implements Listener {

  private ExpBankConfig config;

  public SignChangeUpdateSign(final ExpBankConfig config) {
    this.config = config;
  }

  @EventHandler
  public void onSignChange(SignChangeEvent event) {
    final Location loc = event.getBlock().getLocation();
    World world = loc.getWorld();
    List<Chunk> chunks = Arrays.asList(new Chunk[] { loc.getChunk(),
        world.getChunkAt(loc.add(16.0D, 0.0D, 0.0D)), world.getChunkAt(loc.add(16.0D, 0.0D, 16.0D)),
        world.getChunkAt(loc.add(0.0D, 0.0D, 16.0D)), world.getChunkAt(loc.add(-16.0D, 0.0D, 0.0D)),
        world.getChunkAt(loc.add(-16.0D, 0.0D, -16.0D)),
        world.getChunkAt(loc.add(0.0D, 0.0D, -16.0D)),
        world.getChunkAt(loc.add(16.0D, 0.0D, -16.0D)),
        world.getChunkAt(loc.add(-16.0D, 0.0D, 16.0D)) });

    List<Player> myplayers = new ArrayList<>();

    for (Chunk chunk : chunks) {
      for (Entity entity : chunk.getEntities()) {
        if ((entity instanceof Player) && (!myplayers.contains(entity))) {
          myplayers.add((Player) entity);
        }
      }
    }

    for (final Player user : myplayers) {
      SignHelper.scheduleUpdate(user, loc, config);
    }

    return;
  }
}
