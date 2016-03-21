/**
 *
 */

package com.empcraft.xpbank.dao;

import com.empcraft.xpbank.err.ConfigurationException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Access player and experience data.
 */
public abstract class PlayerExperienceDao extends BaseDao {

  public PlayerExperienceDao(final Connection conn, final FileConfiguration config,
      final Logger logger) {
    super(conn, config, logger);
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
}
