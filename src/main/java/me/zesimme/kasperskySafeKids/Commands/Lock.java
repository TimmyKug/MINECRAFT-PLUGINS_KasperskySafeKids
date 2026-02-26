package me.zesimme.kasperskySafeKids.Commands;

import me.zesimme.kasperskySafeKids.Utils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class Lock implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player p)) {
            return false;
        }

        if (!Utils.isLocationInRegion(p.getLocation())) {
            p.sendMessage(ChatColor.RED + "You cannot use the lock command here.");
            return true;
        }

        String[] temp = args;
        args = new String[temp.length + 1];
        args[0] = p.getName();
        System.arraycopy(temp, 0, args, 1, temp.length);
        PersistentDataContainer data = p.getPersistentDataContainer();
        if (data.has(Utils.lockKey, PersistentDataType.STRING)) {
            data.remove(Utils.lockKey);
            p.sendMessage(ChatColor.YELLOW + "Lock mode disabled!");
        } else {
            data.set(Utils.lockKey, PersistentDataType.STRING, String.join(", ", args).toLowerCase());
            p.sendMessage(ChatColor.GREEN + "Lock mode enabled! Click on anything to lock/unlock it.");
        }

        return true;
    }
}
