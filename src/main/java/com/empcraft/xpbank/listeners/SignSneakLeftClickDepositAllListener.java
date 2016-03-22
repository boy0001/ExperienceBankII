package com.empcraft.xpbank.listeners;

import com.empcraft.xpbank.ExpBankConfig;
import com.empcraft.xpbank.logic.ExpBankPermission;
import com.empcraft.xpbank.logic.PermissionsHelper;
import com.empcraft.xpbank.logic.SignHelper;
import com.empcraft.xpbank.text.MessageUtils;
import com.empcraft.xpbank.text.YamlLanguageProvider;
import com.empcraft.xpbank.threads.ChangeExperienceThread;
import com.google.common.base.Optional;

import org.bukkit.Bukkit;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class SignSneakLeftClickDepositAllListener extends AbstractExperienceSignListener {

  public SignSneakLeftClickDepositAllListener(final YamlLanguageProvider ylp,
      final ExpBankConfig config) {
    super(ylp, config);
  }

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event) {
    if (!isSignForEvent(getConfig(), event, Action.LEFT_CLICK_BLOCK, Optional.<Boolean> absent(),
        Optional.of(new Boolean(true)))) {
      return;
    }

    Player player = event.getPlayer();

    if (!PermissionsHelper.playerHasPermission(player, ExpBankPermission.USE)) {
      MessageUtils.sendMessageToPlayer(player,
          getYlp().getMessage("NOPERM").replace("{STRING}", "expbank.use" + ""));
      return;
    }

    /*
     * Try to depoist everything. The limit check is done in the thread.
     */
    int amountToDeposit = player.getTotalExperience();

    if (amountToDeposit <= 0) {
      MessageUtils.sendMessageToPlayer(player, getYlp().getMessage("EXP-NONE"));

      // nothing to deposit.
      return;
    }

    // CHeck for limit is done in the thread, so we don't need to wait for Database IO.
    ChangeExperienceThread cet = new ChangeExperienceThread(player, amountToDeposit, getConfig(),
        getYlp());
    Bukkit.getScheduler().runTaskAsynchronously(getConfig().getPlugin(), cet);

    // update the sign.
    Sign sign = (Sign) event.getClickedBlock().getState();
    SignHelper.updateSign(player, sign, getConfig());
  }

}
