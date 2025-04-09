package me.zesimme.kasperskySafeKids.Commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import static me.zesimme.kasperskySafeKids.Utils.lockKey;

public class Lock implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player p)) {
            return false;
        }
        String[] temp = args;
        args = new String[temp.length + 1];
        args[0] = p.getDisplayName();
        System.arraycopy(temp, 0, args, 1, temp.length);
        PersistentDataContainer data = p.getPersistentDataContainer();
        if (data.has(lockKey, PersistentDataType.STRING)) {
            data.remove(lockKey);
            p.sendMessage(ChatColor.YELLOW + "Lock mode disabled!");
        } else {
            data.set(lockKey, PersistentDataType.STRING, String.join(", ", args).toLowerCase());
            p.sendMessage(ChatColor.GREEN + "Lock mode enabled! Click on anything to lock/unlock it.");
        }

        return true;
    }
}
