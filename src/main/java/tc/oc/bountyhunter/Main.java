package tc.oc.bountyhunter;

import co.aikar.commands.BukkitCommandManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

  private static Main plugin;
  private Manager manager;

  private BukkitCommandManager commands;

  @Override
  public void onEnable() {
    plugin = this;

    saveDefaultConfig();
    Config.create(getConfig());

    // Setup the command manager and register all commands
    this.commands = new BukkitCommandManager(this);
    commands.registerCommand(new Commands());

    manager = new Manager();

    // Register listener
    Bukkit.getServer().getPluginManager().registerEvents(manager, this);

    getLogger().info("[BountyHunter] BountyHunter has been enabled!");
  }

  @Override
  public void onDisable() {
    plugin = null;
    getLogger().info("[BountyHunter] BountyHunter has been disabled!");
  }

  public static Main get() {
    return plugin;
  }

  public Manager getManager() {
    return manager;
  }
}
