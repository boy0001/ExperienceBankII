/**
 *
 */

package com.empcraft.xpbank.dao;

import com.empcraft.xpbank.ExpBankConfig;
import com.empcraft.xpbank.err.ConfigurationException;

import org.bukkit.entity.Player;

import java.sql.Connection;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Access player and experience data.
 */
public abstract class PlayerExperienceDao extends BaseDao {

  public PlayerExperienceDao(final Connection conn, final ExpBankConfig config) {
    super(conn, config);
  }

  public abstract boolean createTable();

  public abstract boolean insertPlayerAndExperience(Player player, int experience);

  public abstract boolean insertPlayerAndExperience(UUID playerUuid, int experience);

  public abstract int countPlayers();

  public abstract boolean updatePlayerExperience(UUID player, int newExperience);

  /**
   * Returns the saved Exp for all known players.
   * @return the saved exp.
   * @throws ConfigurationException if database could not be read.
   */
  public abstract Map<UUID, Integer> getSavedExperience() throws ConfigurationException;

  protected String getTable() {
    return getConfig().getMySqlUserTable();
  }

  public abstract int getSavedExperience(UUID uniqueId) throws ConfigurationException;

  public abstract boolean updatePlayerExperienceDelta(UUID player, int delta);
}
