package com.empcraft.xpbank.listeners;

import com.empcraft.xpbank.ExpBankConfig;
import com.empcraft.xpbank.logic.DataHelper;
import com.empcraft.xpbank.logic.ExpBankPermission;
import com.empcraft.xpbank.logic.PermissionsHelper;
import com.empcraft.xpbank.logic.SignHelper;
import com.empcraft.xpbank.text.MessageUtils;
import com.empcraft.xpbank.text.Text;
import com.empcraft.xpbank.text.YamlLanguageProvider;
import com.empcraft.xpbank.threads.ChangeExperienceThread;
import com.google.common.base.Optional;

import org.bukkit.Bukkit;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.logging.Level;

public class SignRightClickWithDrawBottleListener extends AbstractExperienceSignListener {
  /**
   * Experience gain per bottle ranges from 3 to 11….
   */
  private static final int EXPERIENCE_PER_BOTTLE = 7;

  public SignRightClickWithDrawBottleListener(YamlLanguageProvider ylp, ExpBankConfig config) {
    super(ylp, config);
  }

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event) {
    if (!isSignForEvent(getConfig(), event, Action.RIGHT_CLICK_BLOCK,
        Optional.of(new Boolean(true)), Optional.<Boolean> absent())) {
      return;
    }

    // Do not throw the bottle...
    event.setCancelled(true);

    Player player = event.getPlayer();

    if (!PermissionsHelper.playerHasPermission(player, ExpBankPermission.USE_BOTTLE)) {
      MessageUtils.sendMessageToPlayer(player,
          getYlp().getMessage(Text.NOPERM).replace("{STRING}",
              ExpBankPermission.USE_BOTTLE.getPermissionNode() + ""));
      return;
    }

    int numberOfBottlesHeld = player.getInventory().getItemInMainHand().getAmount();

    int withdrawAmount = DataHelper.checkForMaximumWithdraw(player,
        numberOfBottlesHeld * EXPERIENCE_PER_BOTTLE, getConfig());

    MessageUtils.sendMessageToPlayer(player, "Withdrawing one bottle.");
    getConfig().getLogger().log(Level.INFO,
        "Player [" + player.getName() + "] is withdrawing one bottle!");

    getConfig().getExperienceCache().substractExperience(player.getUniqueId(), withdrawAmount,
        getConfig(), getYlp());

    ChangeExperienceThread cet = new ChangeExperienceThread(player.getUniqueId(),
        withdrawAmount, getConfig(), getYlp());
    Bukkit.getScheduler().runTaskAsynchronously(getConfig().getPlugin(), cet);

    // update the sign.
    Sign sign = (Sign) event.getClickedBlock().getState();
    SignHelper.updateSign(player, sign, getConfig());
  }
}
