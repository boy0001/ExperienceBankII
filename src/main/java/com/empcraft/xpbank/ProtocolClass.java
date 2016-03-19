package com.empcraft.xpbank;

import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;

public class ProtocolClass {
  ExpBank EXP;
  ProtocolManager protocolmanager = null;

  public void writePacket(PacketContainer packet, String[] lines) {
    WrappedChatComponent[] component = new WrappedChatComponent[4];
    for (int j = 3; j >= 0; j--) {
      if (!lines[j].equals("")) {
        component[j] = WrappedChatComponent.fromJson(lines[j]);
      }
    }

    packet.getChatComponentArrays().write(0, component);
  }

  public ProtocolClass(ExpBank plugin) {
    EXP = plugin;
    protocolmanager = ProtocolLibrary.getProtocolManager();

    protocolmanager.addPacketListener(
        new PacketAdapter(EXP, ListenerPriority.LOW, PacketType.Play.Server.UPDATE_SIGN) {
          public void onPacketSending(PacketEvent event) {
            PacketContainer oldpacket = event.getPacket();
            PacketContainer packet = oldpacket.shallowClone();

            Player player = event.getPlayer();
            WrappedChatComponent[] component = packet.getChatComponentArrays().read(0);

            if (component.length == 0 || component[0] == null) {
              return;
            }

            if (component[0].getJson().contains(EXP.getConfig().getString("text.create"))) {
              String[] lines = new String[4];
              lines[0] = JSONUtil.toJSON(EXP.evaluate(EXP.getConfig().getString("text.1"), player));
              lines[1] = JSONUtil.toJSON(EXP.evaluate(EXP.getConfig().getString("text.2"), player));
              lines[2] = JSONUtil.toJSON(EXP.evaluate(EXP.getConfig().getString("text.3"), player));
              lines[3] = JSONUtil.toJSON(EXP.evaluate(EXP.getConfig().getString("text.4"), player));
              writePacket(packet, lines);
              event.setPacket(packet);
            }

          }
        });
  }
}
