package com.empcraft.xpbank.events;

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

public class SignRightClickWithDrawBottleListener extends AbstractExperienceSignListener {

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
          getYlp().getMessage("NOPERM").replace("{STRING}",
              ExpBankPermission.USE_BOTTLE.getPermissionNode() + ""));
      return;
    }

    int numberOfBottlesHeld = player.getInventory().getItemInMainHand().getAmount();

    // CHeck for limit is done in the thread, so we don't need to wait for Database IO.
    ChangeExperienceThread cet = new ChangeExperienceThread(player, numberOfBottlesHeld,
        getConfig(), getYlp(), true);
    Bukkit.getScheduler().runTaskAsynchronously(getConfig().getPlugin(), cet);

    // update the sign.
    Sign sign = (Sign) event.getClickedBlock().getState();
    SignHelper.updateSign(player, sign, getConfig());
  }
}
