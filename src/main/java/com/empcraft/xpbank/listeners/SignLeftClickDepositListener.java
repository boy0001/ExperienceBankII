package com.empcraft.xpbank.listeners;

import com.empcraft.xpbank.ExpBankConfig;
import com.empcraft.xpbank.logic.DataHelper;
import com.empcraft.xpbank.logic.ExpBankPermission;
import com.empcraft.xpbank.logic.ExperienceLevelCalculator;
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

public class SignLeftClickDepositListener extends AbstractExperienceSignListener {

  public SignLeftClickDepositListener(final YamlLanguageProvider ylp, final ExpBankConfig config) {
    super(ylp, config);
  }

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event) {
    if (!isSignForEvent(getConfig(), event, Action.LEFT_CLICK_BLOCK, Optional.<Boolean> absent(),
        Optional.of(new Boolean(false)))) {
      return;
    }

    Player player = event.getPlayer();

    if (!PermissionsHelper.playerHasPermission(player, ExpBankPermission.USE)) {
      MessageUtils.sendMessageToPlayer(player, getYlp().getMessage(Text.NOPERM).replace("{STRING}",
          ExpBankPermission.USE.getPermissionNode()));
      return;
    }

    /*
     * We need to deposit the delta to the next lower level. If this is just the start of a level,
     * deposit a whole level. If this is in the middle of a level, deposit until we reach the
     * minimum for this level.
     */
    int playerExperience = player.getTotalExperience();
    int amountToDeposit = ExperienceLevelCalculator
        .getExperienceDelteToLowerLevel(playerExperience);

    if (amountToDeposit <= 0) {
      MessageUtils.sendMessageToPlayer(player, getYlp().getMessage(Text.EXP_NONE));

      // nothing to deposit.
      return;
    }

    amountToDeposit = DataHelper.checkForMaximumDeposit(player, amountToDeposit, getConfig());

    MessageUtils.sendMessageToPlayer(player, "Depositing one level: [" + amountToDeposit + "].");
    getConfig().getLogger().log(Level.INFO,
        "Player [" + player.getName() + "] is depositing one level: [" + amountToDeposit + "].");

    getConfig().getExperienceCache().substractExperience(player, amountToDeposit,
        getConfig(), getYlp());

    // update the sign.
    Sign sign = (Sign) event.getClickedBlock().getState();
    SignHelper.updateSign(player, sign, getConfig());
  }
}
