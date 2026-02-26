package me.zesimme.kasperskySafeKids;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;

public class Utils {
    private static KasperskySafeKids plugin;
    public static NamespacedKey lockKey;
    public static NamespacedKey ownersKey;

    public static void init(KasperskySafeKids instance) {
        plugin = instance;
        lockKey = new NamespacedKey(plugin, "lock");
        ownersKey = new NamespacedKey(plugin, "owners");
    }

    public static boolean isLocationInRegion(Location loc) {
        if (loc == null || loc.getWorld() == null || plugin == null)
            return true;

        if (!plugin.getConfig().getBoolean("use-regions", false))
            return true;

        ConfigurationSection regions = plugin.getConfig().getConfigurationSection("regions");
        if (regions == null || regions.getKeys(false).isEmpty()) {
            return true;
        }

        for (String key : regions.getKeys(false)) {
            ConfigurationSection region = regions.getConfigurationSection(key);
            if (region == null)
                continue;

            String worldName = region.getString("world");
            if (worldName != null && !loc.getWorld().getName().equals(worldName)) {
                continue;
            }

            double minX = region.getDouble("min-x", Double.NEGATIVE_INFINITY);
            double maxX = region.getDouble("max-x", Double.POSITIVE_INFINITY);
            double minY = region.getDouble("min-y", Double.NEGATIVE_INFINITY);
            double maxY = region.getDouble("max-y", Double.POSITIVE_INFINITY);
            double minZ = region.getDouble("min-z", Double.NEGATIVE_INFINITY);
            double maxZ = region.getDouble("max-z", Double.POSITIVE_INFINITY);

            double x = loc.getX();
            double y = loc.getY();
            double z = loc.getZ();

            if (x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ) {
                return true;
            }
        }
        return false;
    }
}
