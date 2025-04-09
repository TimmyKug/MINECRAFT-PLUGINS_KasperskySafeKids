package me.zesimme.kasperskySafeKids.Listeners;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.block.TileState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

import static me.zesimme.kasperskySafeKids.Utils.lockKey;
import static me.zesimme.kasperskySafeKids.Utils.ownersKey;

public class LockListener implements Listener {
    @EventHandler(priority = EventPriority.HIGH)
    public void lockBlock(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getPlayer().isSneaking())
            return;
        if (e.isCancelled())
            return;
        Player p = e.getPlayer();
        PersistentDataContainer playerData = p.getPersistentDataContainer();
        if (!playerData.has(lockKey))
            return;
        if (!(e.getClickedBlock().getState() instanceof TileState tileState)) {
            p.sendMessage(ChatColor.RED + "This is not a tile entity.");
            return;
        }
        e.setCancelled(true);

        String ownersString = playerData.get(lockKey, PersistentDataType.STRING);
        PersistentDataContainer blockData = tileState.getPersistentDataContainer();

        if (blockData.has(ownersKey, PersistentDataType.STRING)) {
            blockData.remove(ownersKey);
            tileState.update();
            p.sendMessage(ChatColor.YELLOW + "Unlocked!");
            return;
        }

        blockData.set(ownersKey, PersistentDataType.STRING, ownersString);
        tileState.update();
        p.sendMessage(ChatColor.GREEN + "Locked! Owner(s): " + ownersString);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlaceBlock(BlockPlaceEvent e) {
        if (e.getBlock().getState() instanceof ShulkerBox tileState) {
            PersistentDataContainer data = tileState.getPersistentDataContainer();
            ArrayList<String> owners = new ArrayList<>();
            owners.add(e.getPlayer().getDisplayName().toLowerCase());
            String ownersString = String.join(", ", owners);
            data.set(ownersKey, PersistentDataType.STRING, ownersString);
            tileState.update();
            e.getPlayer().sendMessage(ChatColor.GREEN + "Your Shulkerbox was automatically locked.");
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void lockEntity(EntityDamageByEntityEvent e) {
        if (e.isCancelled())
            return;
        if (!(e.getDamager() instanceof Player p))
            return;
        Entity entity = e.getEntity();
        if (entity instanceof Player)
            return;
        PersistentDataContainer playerData = p.getPersistentDataContainer();
        if (!playerData.has(lockKey))
            return;
        e.setCancelled(true);

        String ownersString = playerData.get(lockKey, PersistentDataType.STRING);
        PersistentDataContainer entityData = entity.getPersistentDataContainer();

        if (entityData.has(ownersKey, PersistentDataType.STRING)) {
            entityData.remove(ownersKey);
            p.sendMessage(ChatColor.YELLOW + "Unlocked!");
            return;
        }

        entityData.set(ownersKey, PersistentDataType.STRING, ownersString);
        p.sendMessage(ChatColor.GREEN + "Locked! Owner(s): " + ownersString);
    }
}
