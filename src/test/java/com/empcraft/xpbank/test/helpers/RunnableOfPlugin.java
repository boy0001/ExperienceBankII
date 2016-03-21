package com.empcraft.xpbank.test.helpers;

import org.bukkit.plugin.Plugin;

public class RunnableOfPlugin {

  private Runnable runnable;

  private Plugin plugin;

  public RunnableOfPlugin(Plugin plugin2, Runnable r) {
    this.plugin = plugin2;
    this.runnable = r;
  }

  public Runnable getRunnable() {
    return runnable;
  }

  public void setRunnable(Runnable runnable) {
    this.runnable = runnable;
  }

  public Plugin getPlugin() {
    return plugin;
  }

  public void setPlugin(Plugin plugin) {
    this.plugin = plugin;
  }

}
