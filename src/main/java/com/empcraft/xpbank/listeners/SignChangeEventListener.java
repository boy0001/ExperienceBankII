/**
 *
 */
package com.empcraft.xpbank.listeners;

import com.empcraft.xpbank.ExpBankConfig;
import com.empcraft.xpbank.logic.ExpBankPermission;
import com.empcraft.xpbank.logic.PermissionsHelper;
import com.empcraft.xpbank.logic.SignHelper;
import com.empcraft.xpbank.text.MessageUtils;
import com.empcraft.xpbank.text.Text;
import com.empcraft.xpbank.text.YamlLanguageProvider;

import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

/**
 * @author bmarwell
 *
 */
public class SignChangeEventListener implements Listener {

  private ExpBankConfig config;
  private YamlLanguageProvider ylp;

  public SignChangeEventListener(final ExpBankConfig config, YamlLanguageProvider ylp) {
    this.config = config;
    this.ylp = ylp;
  }

  @EventHandler
  public void onSignChange(SignChangeEvent event) {
    Player player = event.getPlayer();

    if (!isExpBankSign(event)) {
      // this is not a sign we actually care about.
      return;
    }

    if (!PermissionsHelper.playerHasPermission(player, ExpBankPermission.EXPBANK_CREATE)) {
      // this player does not have the permission to create such a sign.
      event.setLine(0, "&4[ERROR]");
      MessageUtils.sendMessageToPlayer(player,
          ylp.getMessage(Text.NOPERM).replace("{STRING}", "expbank.create" + ""));

      return;
    }

    event.setLine(0, MessageUtils.colorise(config.getExperienceBankActivationString()));
    MessageUtils.sendMessageToPlayer(player, ylp.getMessage(Text.CREATE));

    SignHelper.updateSign(player, (Sign) event.getBlock().getState(), config);
  }

  /**
   * Determines if this sign change event is to be checked.
   *
   * @param event
   *          the sign change event by minecraft.
   * @return true if this sign has [EXP] or so on its top line.
   */
  public boolean isExpBankSign(SignChangeEvent event) {
    String line = ChatColor.stripColor(event.getLine(0)).toLowerCase();
    String expLine = ChatColor.stripColor(config.getExperienceBankActivationString().toLowerCase());

    if (line.contains(expLine)) {
      return true;
    }

    return false;
  }
}
