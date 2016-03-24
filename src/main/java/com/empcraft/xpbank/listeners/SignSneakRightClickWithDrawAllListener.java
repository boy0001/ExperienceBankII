package com.empcraft.xpbank.listeners;

import com.empcraft.xpbank.ExpBankConfig;
import com.empcraft.xpbank.logic.DataHelper;
import com.empcraft.xpbank.logic.ExpBankPermission;
import com.empcraft.xpbank.logic.PermissionsHelper;
import com.empcraft.xpbank.logic.SignHelper;
import com.empcraft.xpbank.text.MessageUtils;
import com.empcraft.xpbank.text.Text;
import com.empcraft.xpbank.text.YamlLanguageProvider;
import com.google.common.base.Optional;

import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.logging.Level;

public class SignSneakRightClickWithDrawAllListener extends AbstractExperienceSignListener {

  public SignSneakRightClickWithDrawAllListener(final YamlLanguageProvider ylp,
      final ExpBankConfig config) {
    super(ylp, config);
  }

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event) {
    if (!isSignForEvent(getConfig(), event, Action.RIGHT_CLICK_BLOCK, Optional.<Boolean> absent(),
        Optional.of(new Boolean(true)))) {
      return;
    }

    Player player = event.getPlayer();

    if (!PermissionsHelper.playerHasPermission(player, ExpBankPermission.USE)) {
      MessageUtils.sendMessageToPlayer(player,
          getYlp().getMessage(Text.NOPERM).replace("{STRING}",
              ExpBankPermission.USE.getPermissionNode()));
      return;
    }

    // there is a check later on in the async thread.
    int withDrawAmount = Integer.MIN_VALUE;

    withDrawAmount = DataHelper.checkForMaximumWithdraw(player, withDrawAmount, getConfig());

    MessageUtils.sendMessageToPlayer(player,
        "Withdrawing all you can get [" + withDrawAmount + "].");
    getConfig().getLogger().log(Level.INFO,
        "Player [" + player.getName() + "] is withdrawing everything he has [" + withDrawAmount
            + "].");

    getConfig().getExperienceCache().substractExperience(player, withDrawAmount,
        getConfig(), getYlp());

    // update the sign.
    Sign sign = (Sign) event.getClickedBlock().getState();
    SignHelper.updateSign(player, sign, getConfig());
  }
}
