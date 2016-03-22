package com.empcraft.xpbank;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.empcraft.xpbank.err.ConfigurationException;
import com.empcraft.xpbank.logic.DataHelper;
import com.empcraft.xpbank.logic.SignHelper;
import com.empcraft.xpbank.text.YamlLanguageProvider;

import org.bukkit.entity.Player;

import java.util.List;
import java.util.logging.Level;

public class SignInterceptor extends PacketAdapter {
  ProtocolManager protocolmanager = null;
  private YamlLanguageProvider ylp;
  private ExpBankConfig config;

  public SignInterceptor(ExpBank plugin, YamlLanguageProvider ylp, ExpBankConfig config) {
    super(plugin, ListenerPriority.LOW, PacketType.Play.Server.UPDATE_SIGN);
    this.ylp = ylp;
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
      int storedPlayerExp = 0;

      try {
        DataHelper dh = new DataHelper(ylp, config);
        storedPlayerExp = dh.getSavedExperience(player);
      } catch (ConfigurationException confEx) {
        config.getLogger().log(Level.WARNING,
            "Could not load experience for player [" + player.getName() + "].", confEx);
      }

      String[] lines = new String[4];
      List<String> signLines = config.getSignContent();

      for (int line = 0; line < 4; line++) {
        String evaluatedLine = SignHelper.renderSignLines(signLines.get(line), player, storedPlayerExp);
        lines[line] = JSONUtil.toJSON(evaluatedLine);
      }

      writePacket(packet, lines);
      event.setPacket(packet);
    }

  }

  public void writePacket(PacketContainer packet, String[] lines) {
    WrappedChatComponent[] component = new WrappedChatComponent[4];

    for (int j = 3; j >= 0; j--) {
      if (!"".equals(lines[j])) {
        component[j] = WrappedChatComponent.fromJson(lines[j]);
      }
    }

    packet.getChatComponentArrays().write(0, component);
  }
}
