package me.zesimme.kasperskySafeKids;

import org.bukkit.NamespacedKey;

public class Utils {
    private static final KasperskySafeKids plugin = KasperskySafeKids.getPlugin(KasperskySafeKids.class);

    public static NamespacedKey lockKey = new NamespacedKey(plugin, "lock");
    public static NamespacedKey ownersKey = new NamespacedKey(plugin, "owners");
}
