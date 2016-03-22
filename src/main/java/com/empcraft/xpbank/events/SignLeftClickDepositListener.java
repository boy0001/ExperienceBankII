package com.empcraft.xpbank.events;

import com.empcraft.xpbank.ExpBankConfig;
import com.empcraft.xpbank.logic.ExpBankPermission;
import com.empcraft.xpbank.logic.ExperienceLevelCalculator;
import com.empcraft.xpbank.logic.PermissionsHelper;
import com.empcraft.xpbank.logic.SignHelper;
import com.empcraft.xpbank.text.MessageUtils;
import com.empcraft.xpbank.text.YamlLanguageProvider;
import com.empcraft.xpbank.threads.ChangeExperienceThread;

import org.bukkit.Bukkit;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class SignLeftClickDepositListener implements Listener {

  private YamlLanguageProvider ylp;
  private ExpBankConfig config;

  public SignLeftClickDepositListener(final YamlLanguageProvider ylp, final ExpBankConfig config) {
    this.ylp = ylp;
    this.config = config;
  }

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event) {
    if (!Action.LEFT_CLICK_BLOCK.equals(event.getAction())) {
      return;
    }

    if (!SignHelper.isExperienceBankSignBlock(event.getClickedBlock(), config)) {
      return;
    }

    Player player = event.getPlayer();

    if (player.isSneaking()) {
      // We only treat deposit one level here.
      return;
    }

    if (!PermissionsHelper.playerHasPermission(player, ExpBankPermission.USE)) {
      MessageUtils.sendMessageToPlayer(player,
          ylp.getMessage("NOPERM").replace("{STRING}", "expbank.use" + ""));
      return;
    }

    /*
     * we need to deposit currentxp - xp for currentlevel - 1;
     */
    int playerExperience = player.getTotalExperience();
    int amountToDeposit = ExperienceLevelCalculator
        .getExperienceDelteToLowerLevel(playerExperience);

    if (amountToDeposit <= 0) {
      MessageUtils.sendMessageToPlayer(player, ylp.getMessage("EXP-NONE"));

      // nothing to do;
      return;
    }

    // CHeck for limit is done in the thread, so we don't need to wait for Database IO.
    ChangeExperienceThread cet = new ChangeExperienceThread(player, amountToDeposit, config, ylp);
    Bukkit.getScheduler().runTaskAsynchronously(config.getPlugin(), cet);

    // update the sign.
    Sign sign = (Sign) event.getClickedBlock().getState();
    SignHelper.updateSign(player, sign, config);
  }
}
