package com.empcraft.xpbank;

import com.empcraft.xpbank.logic.SignHelper;

import org.bukkit.Bukkit;
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

  private ExpBankConfig expBankConfig;

  public InSignsNano(final ExpBankConfig config) {
    Bukkit.getServer().getPluginManager().registerEvents(this, config.getPlugin());
    this.expBankConfig = config;
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
      SignHelper.scheduleUpdate(user, loc, expBankConfig);
    }
    return;
  }

}
