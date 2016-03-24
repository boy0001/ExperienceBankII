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
import com.empcraft.xpbank.threads.ChangeExperienceThread;
import com.google.common.base.Optional;

import org.bukkit.Bukkit;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.logging.Level;

public class SignRightClickWithDrawLevelListener extends AbstractExperienceSignListener {

  public SignRightClickWithDrawLevelListener(final YamlLanguageProvider ylp,
      final ExpBankConfig config) {
    super(ylp, config);
  }

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event) {
    if (!isSignForEvent(getConfig(), event, Action.RIGHT_CLICK_BLOCK, Optional.<Boolean> absent(),
        Optional.of(new Boolean(false)))) {
      return;
    }

    Player player = event.getPlayer();

    if (!PermissionsHelper.playerHasPermission(player, ExpBankPermission.USE)) {
      MessageUtils.sendMessageToPlayer(player,
          getYlp().getMessage(Text.NOPERM).replace("{STRING}",
              ExpBankPermission.USE.getPermissionNode()));
      return;
    }

    int neededForLevel = player.getTotalExperience()
        - ExperienceLevelCalculator.getMinExperienceForLevel(player.getLevel() + 1);

    neededForLevel = DataHelper.checkForMaximumWithdraw(player, neededForLevel, getConfig());
    getConfig().getExperienceCache().substractExperience(player.getUniqueId(), neededForLevel,
        getConfig(), getYlp());

    MessageUtils.sendMessageToPlayer(player, "Withdrawing one level: [" + neededForLevel + "].");
    getConfig().getLogger().log(Level.INFO,
        "Player [" + player.getName() + "] is withdrawing one level: [" + neededForLevel + "].");

    getConfig().getExperienceCache().substractExperience(player.getUniqueId(), neededForLevel,
        getConfig(), getYlp());

    // CHeck for limit is done in the thread, so we don't need to wait for Database IO.
    ChangeExperienceThread cet = new ChangeExperienceThread(player.getUniqueId(), neededForLevel,
        getConfig(),
        getYlp());
    Bukkit.getScheduler().runTaskAsynchronously(getConfig().getPlugin(), cet);

    // update the sign.
    Sign sign = (Sign) event.getClickedBlock().getState();
    SignHelper.updateSign(player, sign, getConfig());
  }
}
