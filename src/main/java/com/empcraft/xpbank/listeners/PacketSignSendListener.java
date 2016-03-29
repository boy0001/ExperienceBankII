package com.empcraft.xpbank.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.empcraft.xpbank.ExpBankConfig;
import com.empcraft.xpbank.logic.SignHelper;

import org.bukkit.entity.Player;

public class PacketSignSendListener extends PacketAdapter {

  private ExpBankConfig config;

  public PacketSignSendListener(ExpBankConfig config) {
    super(config.getPlugin(), ListenerPriority.LOW, PacketType.Play.Server.UPDATE_SIGN);

    this.config = config;
  }

  @Override
  public void onPacketSending(PacketEvent event) {
    PacketContainer oldpacket = event.getPacket();
    PacketContainer packet = oldpacket.shallowClone();

    Player player = event.getPlayer();
    WrappedChatComponent[] component = packet.getChatComponentArrays().read(0);

    if (component.length == 0 || component[0] == null) {
      return;
    }

    if (component[0].getJson().contains(config.getExperienceBankActivationString())) {
      String[] lines = new String[4];
      lines = SignHelper.getSignText(player, config);
      writePacket(packet, lines);
      event.setPacket(packet);
    }
  }

  public void writePacket(PacketContainer packet, String[] lines) {
    WrappedChatComponent[] component = new WrappedChatComponent[4];
    for (int j = 3; j >= 0; j--) {
      if (!lines[j].equals("")) {
        component[j] = WrappedChatComponent.fromJson(lines[j]);
      }
    }
    packet.getChatComponentArrays().write(0, component);
  }

}
