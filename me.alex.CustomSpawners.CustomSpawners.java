package me.alex.CustomSpawners;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import me.alex.CustomSpawners.APIS.Configs.Delay;
import me.alex.CustomSpawners.APIS.Configs.MobType;
import me.alex.CustomSpawners.APIS.Configs.Radius;
import me.alex.CustomSpawners.APIS.Util.Messages;
import me.alex.CustomSpawners.Commands.MainCmd;
import me.alex.CustomSpawners.Events.VanillaEvents.BlockBreakEvent.BBEvent;
import me.alex.CustomSpawners.Events.VanillaEvents.BlockClickEvent.BlockRightClickEvent;
import me.alex.CustomSpawners.Events.VanillaEvents.BlockPlaceEvent.BlockPlaceEvents;
import me.alex.CustomSpawners.Events.VanillaEvents.ChunkLoadAndUnload;
import me.alex.CustomSpawners.Events.VanillaEvents.GuiEvents.InventoryClickListener;
import me.alex.CustomSpawners.Events.VanillaEvents.SpawnerSpawnEvent.SpawnerSpawnEvent;
import me.alex.CustomSpawners.SpawnerManager.SpawnerManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;

public class CustomSpawners extends JavaPlugin
{
  public static CustomSpawners plugin = null;
  public Connection c;
  public List<String> notallowed_spawner_types = new ArrayList();
  public int spawner_block;
  public boolean spawning_enabled = true;
  public Economy econ;
  FileConfiguration data;
  File dfile;
  
  public CustomSpawners() {}
  
  public void onEnable() {
    plugin = this;
    setupConfigs();
    setupEcoWithMessages();
    setupMisc();
    registerEvents();
    addNotAllowedTypes();
  }
  


  public void onDisable()
  {
    plugin = null;
    
    SpawnerManager.getManager().saveAllSpawners(this);
  }
  



  public void registerCommands()
  {
    getCommand("customspawners").setExecutor(new MainCmd(this));
  }
  


  public void registerEvents()
  {
    PluginManager m = Bukkit.getServer().getPluginManager();
    
    if (isEnabled())
    {
      m.registerEvents(new ChunkLoadAndUnload(this), this);
      m.registerEvents(new BlockPlaceEvents(this), this);
      m.registerEvents(new BlockRightClickEvent(this), this);
      m.registerEvents(new BBEvent(), this);
      m.registerEvents(new InventoryClickListener(this), this);
      m.registerEvents(new SpawnerSpawnEvent(), this);
    }
  }
  
  public void msg(Player player, String msg)
  {
    player.sendMessage(Messages.t(msg));
  }
  



  private void setupEcoWithMessages()
  {
    Bukkit.getConsoleSender().sendMessage(Messages.t("&cChecking for vault..."));
    if (setupEconomy())
    {
      Bukkit.getConsoleSender().sendMessage(Messages.t("&eVault found & hooked\n &e-----------------------------"));
    }
    else {
      Bukkit.getConsoleSender().sendMessage(Messages.t("&cVault not found... Disabling plugin\n &e-----------------------------"));
      Bukkit.getPluginManager().disablePlugin(this);
    }
  }
  



  private void addNotAllowedTypes()
  {
    int i = 0;
    
    Bukkit.getConsoleSender().sendMessage(Messages.t("&eAdding Not_Allowed spawner types..."));
    
    for (String str : getConfig().getStringList("Spawners.Settings.Not_Allowed"))
    {

      notallowed_spawner_types.add(str);
      i++;
    }
    





    Bukkit.getConsoleSender().sendMessage(Messages.t(
      "&eFinished & Blocked &a" + i + " &espawner types" + "\n &e-----------------------------"));
  }
  


  private void setupConfigs()
  {
    saveDefaultConfig();
    setup(this);
    Delay.get().setup(this);
    MobType.get().setup(this);
    Radius.get().setup(this);
    

    Bukkit.getConsoleSender().sendMessage(Messages.t("&cConfigs setup\n &e-----------------------------"));
  }
  



  private void setupMisc()
  {
    if (isEnabled())
    {
      registerEvents();
      registerCommands();
    }
    
    spawner_block = getConfig().getInt("Spawners.Settings.Spawner_Block");
    
    SpawnerManager.getManager().loadAllSpawnersFromConfig(this, Boolean.valueOf(isEnabled()));
  }
  



  public void setup(Plugin p)
  {
    if (!p.getDataFolder().exists()) {
      p.getDataFolder().mkdir();
    }
    
    dfile = new File(p.getDataFolder(), "data.yml");
    
    if (!dfile.exists()) {
      try {
        dfile.createNewFile();
      }
      catch (IOException e) {
        Bukkit.getServer().getLogger().severe(ChatColor.RED + "Could not create data.yml!");
      }
    }
    
    data = YamlConfiguration.loadConfiguration(dfile);
  }
  
  public FileConfiguration getData() {
    return data;
  }
  
  public void saveData() {
    try {
      data.save(dfile);
    }
    catch (IOException e) {
      Bukkit.getServer().getLogger().severe(ChatColor.RED + "Could not save data.yml!");
    }
  }
  
  public void reloadData() {
    data = YamlConfiguration.loadConfiguration(dfile);
  }
  



  private boolean setupEconomy()
  {
    if (getServer().getPluginManager().getPlugin("Vault") == null) {
      return false;
    }
    RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
    if (rsp == null) {
      return false;
    }
    econ = ((Economy)rsp.getProvider());
    return econ != null;
  }
}
