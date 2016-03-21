package com.empcraft.xpbank;

import com.empcraft.xpbank.text.MessageUtils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;

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
  private Plugin plugin;
  private int counter = 0;
  private volatile List<Sign> updateQueueSign = new ArrayList<Sign>();
  private volatile List<Player> updateQueuePlayer = new ArrayList<Player>();

  private ExpBankConfig expBankConfig;

  public InSignsNano(Plugin plugin, boolean autoupdating, boolean manualUpdating,
      final ExpBankConfig config) {
    this.manual = manualUpdating;
    Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    this.plugin = plugin;
    this.expBankConfig = config;

    if (autoupdating) {
      Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
        public void run() {
          int size = updateQueuePlayer.size();

          if (size > UPDATE_BUFFER) {
            updateQueuePlayer.remove(size - 1);
            updateQueueSign.remove(size - 1);
            size -= 1;
          }

          ArrayList<Sign> toRemoveSign = new ArrayList<Sign>();
          ArrayList<Player> toRemovePlayer = new ArrayList<Player>();

          for (int i = 0; i < Math.min(UPDATE_AMOUNT, size); i++) {
            if (counter >= size) {
              counter = 0;
            }

            Player player = updateQueuePlayer.get(counter);
            Sign sign = updateQueueSign.get(counter);
            boolean result = updateSign(player, sign);

            if (!result) {
              toRemovePlayer.add(player);
              toRemoveSign.add(sign);
            }
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
    List<Player> myplayers = new ArrayList<Player>();
    for (Chunk chunk : chunks) {
      for (Entity entity : chunk.getEntities()) {
        if (((entity instanceof Player)) && (!myplayers.contains((Player) entity))) {
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
    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
      public void run() {
        if (player.isOnline()) {
          updateAllSigns(player, location);
        }
      }
    }, 5L);
  }

  public void scheduleUpdate(final Player player, final Sign sign, long time) {
    if (brokenSigns.contains(sign.getLocation())) {
      brokenSigns.clear();

      return;
    }

    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
      public void run() {
        if (player.isOnline()) {
          if (sign.getBlock() != null && sign.getBlock().getState() instanceof Sign) {
            updateSign(player, (Sign) sign.getBlock().getState());
          }
        }
      }
    }, time);
  }

  public void addUpdateQueue(Sign sign, Player player) {
    for (int i = 0; i < updateQueuePlayer.size(); i++) {
      if (updateQueuePlayer.get(i).getName().equals(player.getName())) {
        if (updateQueueSign.get(i).getLocation().equals(sign.getLocation())) {
          return;
        }
      }
    }
    updateQueuePlayer.add(player);
    updateQueueSign.add(sign);
  }

  public void updateAllSigns(Player player, Location location) {
    List<BlockState> states = new ArrayList<BlockState>();
    World world = player.getWorld();
    List<Chunk> chunks = Arrays.asList(
        new Chunk[] { location.getChunk(), world.getChunkAt(location.add(16.0D, 0.0D, 0.0D)),
            world.getChunkAt(location.add(16.0D, 0.0D, 16.0D)),
            world.getChunkAt(location.add(0.0D, 0.0D, 16.0D)),
            world.getChunkAt(location.add(-16.0D, 0.0D, 0.0D)),
            world.getChunkAt(location.add(-16.0D, 0.0D, -16.0D)),
            world.getChunkAt(location.add(0.0D, 0.0D, -16.0D)),
            world.getChunkAt(location.add(16.0D, 0.0D, -16.0D)),
            world.getChunkAt(location.add(-16.0D, 0.0D, 16.0D)) });

    for (Chunk chunk : chunks) {
      for (BlockState state : chunk.getTileEntities()) {
        states.add(state);
      }
    }

    for (BlockState current : states) {
      if ((current instanceof Sign)) {
        updateSign(player, (Sign) current);
      }
    }

    states = null;
  }

  public boolean updateSign(Player player, Sign sign) {
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

    String[] lines = MessageUtils.getSignText(sign.getLines(), player, sign,
        expBankConfig,
        (ExpBank) this.plugin);

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
