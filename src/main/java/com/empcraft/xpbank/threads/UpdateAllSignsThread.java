package com.empcraft.xpbank.threads;

import com.empcraft.xpbank.ExpBankConfig;
import com.empcraft.xpbank.logic.SignHelper;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UpdateAllSignsThread implements Runnable {

  private final Player player;
  private final Location location;
  private ExpBankConfig expBankConfig;

  public UpdateAllSignsThread(final Player player, final Location location, final ExpBankConfig expBankConfig) {
    this.player = player;
    this.location = location;
    this.expBankConfig = expBankConfig;
  }

  @Override
  public void run() {
    if (null == player || !player.isOnline()) {
      return;
    }

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
        SignHelper.updateSign(player, (Sign) current, expBankConfig);
      }
    }

    states = null;
  }

}
