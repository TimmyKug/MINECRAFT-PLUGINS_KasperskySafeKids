package me.zesimme.kasperskySafeKids.Listeners;

import org.bukkit.ChatColor;
import org.bukkit.block.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

import static me.zesimme.kasperskySafeKids.Utils.ownersKey;

public class UseListener implements Listener {
    //Blockhandlers
    @EventHandler(priority = EventPriority.LOW)
    public void onUseBlock(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getPlayer().isSneaking())
            return;
        Block b = e.getClickedBlock();
        Player p = e.getPlayer();

        if (isLockedBlock(p.getDisplayName(), b)) {
            cancel(e, p);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onBlockBreak(BlockBreakEvent e) {
        if (isLockedBlock(e.getPlayer().getDisplayName(), e.getBlock())) {
            cancel(e, e.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onExplode(EntityExplodeEvent e) {
        e.blockList().removeIf(b -> isLockedBlock("", b));
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onHopperSuction(InventoryMoveItemEvent e) {
        if (!(e.getDestination().getHolder() instanceof Hopper h))
            return;
        TileState hopper = (TileState) h.getBlock().getState();
        PersistentDataContainer data = hopper.getPersistentDataContainer();
        TileState source = (TileState) e.getSource().getLocation().getBlock().getState();

        if (!data.has(ownersKey, PersistentDataType.STRING)) {
            if (isLockedBlock("", source.getBlock()))
                cancel(e, null);
            return;
        }

        String[] owners = data.get(ownersKey, PersistentDataType.STRING).split(", ");
        for (String owner : owners) {
            if (isLockedBlock(owner, source.getBlock())) {
                cancel(e, null);
                return;
            }
        }
    }


    //Entity handlers
    @EventHandler(priority = EventPriority.LOW)
    public void onUseEntity(PlayerInteractEntityEvent e) {
        if (isLockedEntity(e.getPlayer().getDisplayName(), e.getRightClicked())) {
            cancel(e, e.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onEntityDamage(EntityDamageEvent e) {
        if (isLockedEntity("", e.getEntity()))
            cancel(e, null);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player p))
            return;
        if (isLockedEntity(p.getDisplayName(), e.getEntity())) {
            cancel(e, p);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onEntityPickup(EntityPickupItemEvent e) {
        if (isLockedEntity("", e.getEntity()))
            cancel(e, null);
    }

    //Helpers
    public boolean isLockedBlock(String p, Block b) {
        if (b == null || !(b.getState() instanceof TileState tileState)) {
            return false;
        }

        if (b.getState() instanceof Container c && c.getInventory().getHolder() instanceof DoubleChest dbc)
            return isLocked(p, ((Chest) dbc.getLeftSide()).getPersistentDataContainer()) || isLocked(p, ((Chest) dbc.getRightSide()).getPersistentDataContainer());

        return isLocked(p, tileState.getPersistentDataContainer());
    }

    public boolean isLockedEntity(String p, Entity e) {
        return isLocked(p, e.getPersistentDataContainer());
    }

    public boolean isLocked(String player, PersistentDataContainer data) {
        if (!data.has(ownersKey))
            return false;

        List<String> owners = List.of(data.get(ownersKey, PersistentDataType.STRING).split(", "));

       return !owners.contains(player.toLowerCase());
    }

    public void cancel(Cancellable e, Player p) {
        if (p == null) {
            e.setCancelled(true);
            return;
        }
        if (p.isOp()) {
            p.sendMessage(ChatColor.RED + "You just bypassed a lock because you are OP");
            return;
        }
        e.setCancelled(true);
        p.sendMessage(ChatColor.RED + "This belongs to someone else");
    }
}
