/**
 *
 */
package com.empcraft.xpbank.dao.impl.yaml;

import code.husky.DatabaseConnectorException;

import com.empcraft.xpbank.ExpBankConfig;
import com.empcraft.xpbank.dao.PlayerExperienceDao;
import com.google.common.io.Files;

import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 *
 */
public class YamlPlayerExperienceDao extends PlayerExperienceDao {

  public YamlPlayerExperienceDao(Connection conn, ExpBankConfig config) {
    super(conn, config);
  }

  @Override
  public boolean createTable() {
    boolean exists = false;
    File xpdir = getXpFolder();

    if (!xpdir.exists()) {
      exists = xpdir.mkdirs();
    }

    return exists;
  }

  private File getXpFolder() {
    File xpdir = new File(getConfig().getPlugin().getDataFolder(), getTable());

    return xpdir;
  }

  private File getXpFileForPlayer(final UUID playerUuid) {
    File playerXpFile = new File(getXpFolder(), playerUuid.toString() + ".xp");

    return playerXpFile;
  }

  @Override
  public boolean insertPlayerAndExperience(Player player, int experience) {
    return this.insertPlayerAndExperience(player.getUniqueId(), experience);
  }

  @Override
  public boolean insertPlayerAndExperience(UUID playerUuid, int experience) {
    File plXpFile = getXpFileForPlayer(playerUuid);
    try {
      Files.write(Integer.toString(experience), plXpFile, Charset.forName("UTF-8"));
    } catch (IOException ioEx) {
      getConfig().getLogger().log(Level.SEVERE,
          "Could not write experience for uuid [{}].".replaceFirst("\\{\\}", playerUuid.toString()),
          ioEx);
    }
    return super.insertPlayerAndExperience(playerUuid, experience);
  }

  @Override
  public int countPlayers() {
    // TODO Auto-generated method stub
    return super.countPlayers();
  }

  @Override
  public boolean updatePlayerExperience(UUID player, int newExperience) {
    // TODO Auto-generated method stub
    return super.updatePlayerExperience(player, newExperience);
  }

  @Override
  public boolean updatePlayerExperienceDelta(UUID player, int delta) {
    // TODO Auto-generated method stub
    return super.updatePlayerExperienceDelta(player, delta);
  }

  @Override
  public Map<UUID, Integer> getSavedExperience() throws DatabaseConnectorException {
    // TODO Auto-generated method stub
    return super.getSavedExperience();
  }

  @Override
  public int getSavedExperience(UUID uniqueId) throws DatabaseConnectorException {
    int experience = 0;
    File playerXpFile = getXpFileForPlayer(uniqueId);

    try {
      String firstLine = Files.readFirstLine(playerXpFile, Charset.forName("UTF-8"));
      experience = NumberUtils.toInt(firstLine, 0);
    } catch (IOException ioEx) {
      getConfig().getLogger().log(Level.SEVERE,
          "Could not load experience for uuid [{}].".replaceFirst("\\{\\}", uniqueId.toString()),
          ioEx);
    }

    return experience;
  }

  @Override
  public boolean insertNewPlayer(UUID uuid) {
    // TODO Auto-generated method stub
    return super.insertNewPlayer(uuid);
  }

  @Override
  public String getSqlInsert() {
    // TODO Auto-generated method stub
    return super.getSqlInsert();
  }

}
