package me.zesimme.kasperskySafeKids;

import me.zesimme.kasperskySafeKids.Commands.Lock;
import me.zesimme.kasperskySafeKids.Listeners.LockListener;
import me.zesimme.kasperskySafeKids.Listeners.UseListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class KasperskySafeKids extends JavaPlugin {

    @Override
    public void onEnable() {
        getCommand("lock").setExecutor(new Lock());
        getServer().getPluginManager().registerEvents(new UseListener(), this);
        getServer().getPluginManager().registerEvents(new LockListener(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
