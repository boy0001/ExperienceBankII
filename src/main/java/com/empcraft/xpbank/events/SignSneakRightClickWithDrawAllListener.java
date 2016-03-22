package com.empcraft.xpbank.events;

import com.empcraft.xpbank.ExpBankConfig;
import com.empcraft.xpbank.logic.ExpBankPermission;
import com.empcraft.xpbank.logic.PermissionsHelper;
import com.empcraft.xpbank.logic.SignHelper;
import com.empcraft.xpbank.text.MessageUtils;
import com.empcraft.xpbank.text.YamlLanguageProvider;
import com.empcraft.xpbank.threads.ChangeExperienceThread;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class SignSneakRightClickWithDrawAllListener extends AbstractExperienceSignListener {

  public SignSneakRightClickWithDrawAllListener(final YamlLanguageProvider ylp,
      final ExpBankConfig config) {
    super(ylp, config);
  }

  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event) {
    if (!Action.RIGHT_CLICK_BLOCK.equals(event.getAction())) {
      return;
    }

    if (!SignHelper.isExperienceBankSignBlock(event.getClickedBlock(), getConfig())) {
      return;
    }

    Player player = event.getPlayer();

    if (player.getInventory().getItemInMainHand().getType() == Material.GLASS_BOTTLE) {
      return;
    }

    if (!player.isSneaking()) {
      // We only treat withdraw all here.
      return;
    }

    if (!PermissionsHelper.playerHasPermission(player, ExpBankPermission.USE)) {
      MessageUtils.sendMessageToPlayer(player,
          getYlp().getMessage("NOPERM").replace("{STRING}", "expbank.use" + ""));
      return;
    }

    // there is a check later on in the async thread.
    int withDrawAmount = Integer.MIN_VALUE;

    // CHeck for limit is done in the thread, so we don't need to wait for Database IO.
    ChangeExperienceThread cet = new ChangeExperienceThread(player, withDrawAmount, getConfig(),
        getYlp());
    Bukkit.getScheduler().runTaskAsynchronously(getConfig().getPlugin(), cet);

    // update the sign.
    Sign sign = (Sign) event.getClickedBlock().getState();
    SignHelper.updateSign(player, sign, getConfig());
  }
}
