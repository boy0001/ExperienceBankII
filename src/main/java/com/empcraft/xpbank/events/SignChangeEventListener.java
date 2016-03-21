/**
 *
 */
package com.empcraft.xpbank.events;

import com.empcraft.xpbank.InSignsNano;
import com.empcraft.xpbank.YamlLanguageProvider;
import com.empcraft.xpbank.logic.PermissionsHelper;
import com.empcraft.xpbank.text.MessageUtils;

import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

/**
 * @author bmarwell
 *
 */
public class SignChangeEventListener implements Listener {

  private FileConfiguration config;
  private YamlLanguageProvider ylp;
  private InSignsNano signsListener;

  public SignChangeEventListener(final InSignsNano signsListener, final FileConfiguration config,
      YamlLanguageProvider ylp) {
    this.signsListener = signsListener;
    this.config = config;
    this.ylp = ylp;
  }

  @EventHandler
  public void onSignChange(SignChangeEvent event) {
    String line = ChatColor.stripColor(event.getLine(0)).toLowerCase();
    String expLine = ChatColor.stripColor(config.getString("text.create").toLowerCase());

    if (line.contains(expLine)) {
      Player player = event.getPlayer();

      if (PermissionsHelper.playerHasPermission(player, "expbank.create")) {
        event.setLine(0, MessageUtils.colorise(config.getString("text.create")));
        MessageUtils.sendMessageToPlayer(player, ylp.getMessage("CREATE"));
      } else {
        event.setLine(0, "&4[ERROR]");
        MessageUtils.sendMessageToPlayer(player,
            ylp.getMessage("NOPERM").replace("{STRING}", "expbank.create" + ""));
      }

      signsListener.scheduleUpdate(player, (Sign) event.getBlock().getState(), 6);
    }
  }
}
