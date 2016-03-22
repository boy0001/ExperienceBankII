package com.empcraft.xpbank;

import com.empcraft.xpbank.logic.SignHelper;
import com.empcraft.xpbank.threads.UpdateAllSignsThread;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/*
 * Copyright (c) 2014 Jesse Boyd (http://empcraft.com/)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

public class InSignsNano implements Listener {
  // BUFFER = how many signs can be in the queue to auto-update at one time
  // lower it if you want to save a couple kilobytes of ram
  private static final int UPDATE_BUFFER = 4096;

  // AMOUNT = how many signs (MAX) to update each tick
  private static final int UPDATE_AMOUNT = 16;

  private final HashSet<Location> brokenSigns = new HashSet<>();

  private boolean manual;
  private int counter = 0;
  private volatile List<Sign> updateQueueSign = new ArrayList<>();
  private volatile List<Player> updateQueuePlayer = new ArrayList<>();

  private ExpBankConfig expBankConfig;

  public InSignsNano(boolean autoupdating, boolean manualUpdating, final ExpBankConfig config) {
    this.manual = manualUpdating;
    Bukkit.getServer().getPluginManager().registerEvents(this, config.getPlugin());
    this.expBankConfig = config;

    if (autoupdating) {
      Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(config.getPlugin(),
          new Runnable() {
            @Override
            public void run() {
              int size = updateQueuePlayer.size();

              if (size > UPDATE_BUFFER) {
                updateQueuePlayer.remove(size - 1);
                updateQueueSign.remove(size - 1);
                size -= 1;
              }

              ArrayList<Sign> toRemoveSign = new ArrayList<>();
              ArrayList<Player> toRemovePlayer = new ArrayList<>();

              for (int i = 0; i < Math.min(UPDATE_AMOUNT, size); i++) {
                if (counter >= size) {
                  counter = 0;
                }

                Player player = updateQueuePlayer.get(counter);
                Sign sign = updateQueueSign.get(counter);
                SignHelper.updateSign(player, sign, expBankConfig);

                toRemovePlayer.add(player);
                toRemoveSign.add(sign);
                counter++;
              }

              updateQueuePlayer.removeAll(toRemovePlayer);
              updateQueueSign.removeAll(toRemoveSign);
            }
          }, 0L, 1L);
    }
  }

  public Set<Location> getBrokenSigns() {
    return brokenSigns;
  }

  public boolean addBrokenSign(Location loc) {
    return brokenSigns.add(loc);
  }

  @EventHandler
  public void onPlayerMove(PlayerMoveEvent event) {
    if (!event.getFrom().getChunk().equals(event.getTo().getChunk())) {
      scheduleUpdate(event.getPlayer(), event.getTo());
    }
  }

  @EventHandler
  public void onPlayerTeleport(PlayerTeleportEvent event) {
    if (!event.getFrom().getChunk().equals(event.getTo().getChunk())) {
      scheduleUpdate(event.getPlayer(), event.getTo());
    }
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
      scheduleUpdate(user, loc);
    }
    return;
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    scheduleUpdate(event.getPlayer(), event.getPlayer().getLocation());
  }

  public void scheduleUpdate(final Player player, final Location location) {
    if (!manual) {
      return;
    }

    // manual update.
    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(expBankConfig.getPlugin(),
        new UpdateAllSignsThread(player, location, expBankConfig), 5L);
  }

  public void addUpdateQueue(Sign sign, Player player) {
    for (int i = 0; i < updateQueuePlayer.size(); i++) {
      if (updateQueuePlayer.get(i).getName().equals(player.getName())
          && updateQueueSign.get(i).getLocation().equals(sign.getLocation())) {
          return;
      }
    }

    updateQueuePlayer.add(player);
    updateQueueSign.add(sign);
  }

}
