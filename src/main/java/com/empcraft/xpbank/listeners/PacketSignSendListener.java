package com.empcraft.xpbank.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.empcraft.xpbank.ExpBankConfig;
import com.empcraft.xpbank.events.SignSendEvent;
import com.empcraft.xpbank.logic.UpdateSignPacketUtility;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.logging.Level;

public class PacketSignSendListener extends PacketAdapter {

  private ExpBankConfig config;

  public PacketSignSendListener(ExpBankConfig config) {
    super(config.getPlugin(), ListenerPriority.LOW, PacketType.Play.Server.UPDATE_SIGN);

    this.config = config;
  }

  /*
   * (non-Javadoc)
   *
   * @see com.comphenix.protocol.events.PacketAdapter#onPacketSending(com.comphenix.protocol.events.
   * PacketEvent) Source:
   * https://github.com/blablubbabc/IndividualSigns/blob/master/src/main/java/de/blablubbabc/insigns
   * /InSigns.java
   *
   */
  @Override
  public void onPacketSending(PacketEvent event) {
    PacketContainer signUpdatePacket = event.getPacket();
    Player player = event.getPlayer();
    BlockPosition blockPosition = UpdateSignPacketUtility.getLocation(signUpdatePacket);
    Location location = new Location(player.getWorld(), blockPosition.getX(), blockPosition.getY(),
        blockPosition.getZ());

    // call the SignSendEvent:
    SignSendEvent signSendEvent = new SignSendEvent(player, location,
        UpdateSignPacketUtility.getLinesAsStrings(signUpdatePacket));
    config.getLogger().log(Level.FINER, "Sending sign send event!");
    Bukkit.getPluginManager().callEvent(signSendEvent);

    if (signSendEvent.isCancelled()) {
      event.setCancelled(true);
    } else {
      // only replace the outgoing packet if it is needed:
      if (signSendEvent.isModified()) {
        String[] lines = signSendEvent.getLines();

        // prepare new outgoing packet:
        PacketContainer outgoingPacket = signUpdatePacket.shallowClone();
        UpdateSignPacketUtility.setLinesFromStrings(outgoingPacket, lines);

        event.setPacket(outgoingPacket);
      }
    }
  }

}
