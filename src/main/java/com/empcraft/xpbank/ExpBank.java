package com.empcraft.xpbank;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import code.husky.mysql.MySQL;

public class ExpBank extends JavaPlugin implements Listener {
	ExpBank plugin;
	private static YamlConfiguration exp;
    private static File expFile;
    private InSignsNano ISN;
	private Connection connection;
	private Statement statement;
	private YamlConfiguration langYAML;
	
	public HashMap<UUID, Integer> exp_map = new HashMap<>();
	
	public final String version = getDescription().getVersion();
	String evaluate(String mystring, Player player) {
		if (mystring.contains("{player}")) {
			mystring = mystring.replace("{player}", player.getName());
		}
		if (mystring.contains("{expbank}")) {
			mystring = mystring.replace("{expbank}", ""+getExp(player.getUniqueId()));
		}
		ExperienceManager expMan = new ExperienceManager(player);
		if (mystring.contains("{exp}")) {
			mystring = mystring.replace("{exp}", ""+expMan.getCurrentExp());
		}
		if (mystring.contains("{lvl}")) {
			mystring = mystring.replace("{lvl}", ""+player.getLevel());
		}
		if (mystring.contains("{lvlbank}")) {
			mystring = mystring.replace("{lvlbank}", ""+expMan.getLevelForExp(getExp(player.getUniqueId())));
		}
		if (mystring.contains("{lvlbank2}")) {
			mystring = mystring.replace("{lvlbank2}", ""+(expMan.getLevelForExp(expMan.getCurrentExp()+getExp(player.getUniqueId()))-player.getLevel()));
		}
		return colorise(mystring);
	}
	String colorise(String mystring) {
    	return ChatColor.translateAlternateColorCodes('&', mystring);
    }
	public String getMessage(String key) {
		try {
			return colorise(langYAML.getString(key));
		}
		catch (Exception e){
			return "";
		}
	}
	
	@Override
	public void onDisable() {
	    saveConfig();
	}
	
	@Override
	public void onEnable(){
		msg(null,"&8===&a[&7EXPBANK&a]&8===");
		plugin = this;
		expFile = new File(getDataFolder()+File.separator+"xplist.yml");
		exp = YamlConfiguration.loadConfiguration(expFile);
		Map<String, Object> options = new HashMap<String, Object>();
		saveResource("english.yml", true);
		saveResource("spanish.yml", true);
		saveResource("catalan.yml", true);
		getConfig().set("version", version);
		options.put("language","english");
		options.put("storage.default",825);
        options.put("text.create","[EXP]");
        options.put("text.1","&8---&aEXP&8---");
        options.put("text.2","{player}");
        options.put("text.3","{expbank}");
        options.put("text.4","&8---&a===&8---");
        options.put("mysql.enabled",false);
        options.put("mysql.connection.port",3306);
        options.put("mysql.connection.host","localhost");
        options.put("mysql.connection.username","root");
        options.put("mysql.connection.password","");
        options.put("mysql.connection.database","mysql");
        options.put("mysql.connection.table","expbank");
        for (final Entry<String, Object> node : options.entrySet()) {
        	 if (!getConfig().contains(node.getKey())) {
        		 getConfig().set(node.getKey(), node.getValue());
        	 }
        }
        saveConfig();
        langYAML = YamlConfiguration.loadConfiguration(new File(getDataFolder(), getConfig().getString("language").toLowerCase()+".yml"));
        if (getConfig().getBoolean("mysql.enabled")) {
        	msg(null,getMessage("MYSQL"));
        	MySQL MySQL = new MySQL(plugin, getConfig().getString("mysql.connection.host"), getConfig().getString("mysql.connection.port"), getConfig().getString("mysql.connection.database"), getConfig().getString("mysql.connection.username"), getConfig().getString("mysql.connection.password"));
        	try {
				connection = MySQL.openConnection();
				msg(null,getMessage("SUCCESS"));
				statement = connection.createStatement();
				statement.executeUpdate("CREATE TABLE IF NOT EXISTS "+getConfig().getString("mysql.connection.table")+" ( UUID VARCHAR(36), EXP INT )");
				ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM "+getConfig().getString("mysql.connection.table"));
				int length = 0;
				if(result.next()){
					length = result.getInt(1);
				}
				if (length==0) {
					Set<String> players = exp.getKeys(false);
					if (players.size()>0) {
						msg(null,getMessage("CONVERT"));
						for (String player:players) {
							statement.executeUpdate("INSERT INTO "+getConfig().getString("mysql.connection.table")+" VALUES('"+player+"',"+exp.get(player)+")");
						}
						msg(null,getMessage("DONE"));
						exp = null;
						expFile = null;
					}
				}
				
				
	                result = statement.executeQuery("SELECT UUID, EXP FROM "+getConfig().getString("mysql.connection.table")+";");
	                while (result.next()) {
	                    try {
    	                    int experience = result.getInt("EXP");
    	                    String uuid_s = result.getString("UUID");
    	                    UUID uuid = UUID.fromString(uuid_s);
    	                    exp_map.put(uuid, experience);
	                    }
	                    catch (Exception e) {
	                    }
	                }
			} catch (Exception e) {
				e.printStackTrace();
				connection = null;
 				msg(null,getMessage("MYSQL-CONNECT"));
			}
        }
        else {
            Set<String> players = exp.getKeys(false);
            for (String player : players) {
                try {
                    int experience = exp.getInt(player);
                    UUID uuid = UUID.fromString(player);
                    exp_map.put(uuid, experience);
                }
                catch (Exception e) {
                }
            }
        	msg(null,getMessage("YAML"));
        }
        
        boolean manual = true;
        Plugin protocolPlugin = Bukkit.getServer().getPluginManager().getPlugin("ProtocolLib");
        if((protocolPlugin != null)) {
            if (protocolPlugin.isEnabled()) {
                msg(null,"&aUsing ProtocolLib for packets");
                new ProtocolClass(plugin);
                manual = false;
            }
        }
        
		ISN = new InSignsNano(plugin, false, manual) {
			@Override
			public String[] getValue(String[] lines, Player player, Sign sign) {
				if (lines[0].equals(colorise(getConfig().getString("text.create")))) {
					lines[0] = evaluate(getConfig().getString("text.1"),player);
					lines[1] = evaluate(getConfig().getString("text.2"),player);
					lines[2] = evaluate(getConfig().getString("text.3"),player);
					lines[3] = evaluate(getConfig().getString("text.4"),player);
				}
				return lines;
			}
		};
		Bukkit.getServer().getPluginManager().registerEvents(this, this);
        BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
        
        // Save any changes to the config
        scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                saveConfig();
            }
        }, 24000L, 24000L);
	}
	
	private void runTask(final Runnable r) {
        Bukkit.getScheduler().runTaskAsynchronously(this, r);
    }
	
    @EventHandler
	public void onSignChange(SignChangeEvent event) {
    	String line = ChatColor.stripColor(event.getLine(0)).toLowerCase();
    	String expLine = ChatColor.stripColor(getConfig().getString("text.create").toLowerCase());
    	if (line.contains(expLine)) {
    		Player player = event.getPlayer();
    		if (checkperm(player, "expbank.create")) {
    			event.setLine(0, colorise(getConfig().getString("text.create")));
    			msg(player, getMessage("CREATE"));
    		}
    		else {
    			event.setLine(0,"&4[ERROR]");
    			msg(player,getMessage("NOPERM").replace("{STRING}","expbank.create"+""));
    		}
    		ISN.scheduleUpdate(player, (Sign) event.getBlock().getState(), 6);
    	}
    }
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if ((block.getType() == Material.SIGN_POST) || (block.getType() == Material.WALL_SIGN)) {
            Sign sign = (Sign)block.getState();
            if (sign.getLine(0).equals(colorise(getConfig().getString("text.create")))) {
                ISN.broken_signs.add(event.getBlock().getLocation());
            }
        }
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
    	if (!(event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK)) {
	    	  return;
		}
		Block block = event.getClickedBlock();
		if ((block.getType() == Material.SIGN_POST) || (block.getType() == Material.WALL_SIGN)) {
			Sign sign = (Sign)block.getState();
			Player player = event.getPlayer();
			String[] lines = sign.getLines();
			if (lines[0].equals(colorise(getConfig().getString("text.create")))) {
				if (checkperm(player,"expbank.use")) {
					ExperienceManager expMan = new ExperienceManager(player);
					int amount;
					int myExp = getExp(player.getUniqueId());
					if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
						if (player.isSneaking()) {
							amount = myExp;
						}
						else {
							amount = expMan.getXpForLevel(expMan.getLevelForExp(expMan.getCurrentExp())+1)-expMan.getCurrentExp();
							if (amount > myExp) {
								amount = myExp;
							}
						}
						if (player.getInventory().getItemInMainHand().getType()==Material.GLASS_BOTTLE && checkperm(player,"expbank.use.bottle")) {
							int bottles = player.getInventory().getItemInMainHand().getAmount();
							if (bottles*7>myExp) {
								msg(player,getMessage("BOTTLE-ERROR"));
								return;
							}
							else {
								amount = bottles*7;
								player.getInventory().getItemInMainHand().setType(Material.EXP_BOTTLE);
								event.setCancelled(true);
							}
						}
						else {
							expMan.changeExp(amount);
						}
					}
					else {
						if (player.isSneaking()) {
							amount = -expMan.getCurrentExp();
						}
						else {
							if (expMan.getCurrentExp()>17) {
								amount = -(expMan.getCurrentExp()-expMan.getXpForLevel(expMan.getLevelForExp(expMan.getCurrentExp())-1));
							}
							else {
								amount = -expMan.getCurrentExp();
							}
						}
						int max = getMaxExp(player);
						if (amount==0) {
							msg(player,getMessage("EXP-NONE"));
						}
						else if (myExp-amount>max) {
							amount = -(max-myExp);
							if (amount==0) { msg(player,getMessage("EXP-LIMIT")); }
						}
						expMan.changeExp(amount);
					}
					changeExp(player.getUniqueId(), -amount);
					ISN.scheduleUpdate(player, sign, 1);
				}
				else {
					msg(player,getMessage("NOPERM").replace("{STRING}","expbank.use"+""));
	    		}
			}
		}
    }
    public int getMaxExp(Player player) {
    	Set<String> nodes = getConfig().getConfigurationSection("storage").getKeys(false);
    	int max = 0;
    	for (String perm:nodes) {
    		if (perm.equals("default") || checkperm(player, "expbank.limit."+perm)) {
    			int value = getConfig().getInt("storage."+perm);
    			if (value>max) {
    				max = value;
    			}
    		}
    	}
    	return max;
    }
	public int getExp(UUID uuid) {
	    Integer value = exp_map.get(uuid);
	    if (value == null) {
	        return 0;
	    }
	    return value;
	}
	public void changeExp(final UUID uuid, final int value) {
		if (exp==null) {
		    runTask(new Runnable() {
                @Override
                public void run() {
                    try {
                        statement.executeUpdate("UPDATE "+getConfig().getString("mysql.connection.table")+" SET EXP=EXP+"+value+" WHERE UUID='"+uuid.toString()+"'");
                    } catch (SQLException e) {
                        msg(null,getMessage("MYSQL-GET"));
                    }
                    return;
                }
            });
		}
		else {
		    exp.set(uuid.toString(), value+getExp(uuid));
		    try { exp.save(expFile); } catch (IOException e) { }
		}
		exp_map.put(uuid, getExp(uuid) + value);
	}
	public boolean checkperm(Player player,String perm) {
    	String[] nodes = perm.split("\\.");
    	String node = "";
    	if (player==null) { return true; }
    	else if (player.hasPermission(perm)) { return true; }
    	else if (player.isOp()==true) { return true; }
    	else {
    		for(int i = 0; i < nodes.length-1; i++) {
    			node+=nodes[i]+".";
            	if (player.hasPermission(node+"*")) { return true; }
    		}
    	}
		return false;
    }
    public void msg(Player player,String mystring) {
    	if (mystring.equals("")) { return; }
    	if (player==null) { getServer().getConsoleSender().sendMessage(colorise(mystring)); }
    	else { player.sendMessage(colorise(mystring)); }
    }
}
