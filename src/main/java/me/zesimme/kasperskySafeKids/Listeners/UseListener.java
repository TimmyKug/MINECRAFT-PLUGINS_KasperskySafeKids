package me.zesimme.kasperskySafeKids.Listeners;

import me.zesimme.kasperskySafeKids.Utils;
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
import org.bukkit.inventory.Inventory;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class UseListener implements Listener {
    // Blockhandlers
    @EventHandler(priority = EventPriority.LOW)
    public void onUseBlock(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getPlayer().isSneaking())
            return;
        Block b = e.getClickedBlock();
        Player p = e.getPlayer();

        if (b != null && Utils.isLocationInRegion(b.getLocation()) && isLockedBlock(p.getName(), b)) {
            cancel(e, p);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onBlockBreak(BlockBreakEvent e) {
        if (Utils.isLocationInRegion(e.getBlock().getLocation())
                && isLockedBlock(e.getPlayer().getName(), e.getBlock())) {
            cancel(e, e.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onExplode(EntityExplodeEvent e) {
        e.blockList().removeIf(b -> Utils.isLocationInRegion(b.getLocation()) && isLockedBlock("", b));
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onHopperSuction(InventoryMoveItemEvent e) {
        Inventory sourceInv = e.getSource();

        if (sourceInv.getLocation() != null && !Utils.isLocationInRegion(sourceInv.getLocation())) {
            return;
        }

        PersistentDataContainer sourceData = getPDC(sourceInv);
        if (sourceData == null)
            return;

        PersistentDataContainer destData = getPDC(e.getDestination());

        if (!sourceData.has(Utils.ownersKey)) {
            return; // Source is not locked
        }

        if (destData == null || !destData.has(Utils.ownersKey)) {
            // Source is locked, dest is not locked -> prevent theft
            cancel(e, null);
            return;
        }

        String[] destOwners = destData.get(Utils.ownersKey, PersistentDataType.STRING).split(", ");
        for (String destOwner : destOwners) {
            if (isLocked(destOwner, sourceData)) {
                // If the dest owner does NOT have access to the source, prevent theft.
                cancel(e, null);
                return;
            }
        }
    }

    // Entity handlers
    @EventHandler(priority = EventPriority.LOW)
    public void onUseEntity(PlayerInteractEntityEvent e) {
        if (Utils.isLocationInRegion(e.getRightClicked().getLocation())
                && isLockedEntity(e.getPlayer().getName(), e.getRightClicked())) {
            cancel(e, e.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onEntityDamage(EntityDamageEvent e) {
        if (Utils.isLocationInRegion(e.getEntity().getLocation()) && isLockedEntity("", e.getEntity()))
            cancel(e, null);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player p))
            return;
        if (Utils.isLocationInRegion(e.getEntity().getLocation()) && isLockedEntity(p.getName(), e.getEntity())) {
            cancel(e, p);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onEntityPickup(EntityPickupItemEvent e) {
        if (Utils.isLocationInRegion(e.getEntity().getLocation()) && isLockedEntity("", e.getEntity()))
            cancel(e, null);
    }

    // Helpers
    public PersistentDataContainer getPDC(Inventory inv) {
        if (inv == null || inv.getHolder() == null)
            return null;
        if (inv.getHolder() instanceof BlockState state && state instanceof TileState tile) {
            return tile.getPersistentDataContainer();
        } else if (inv.getHolder() instanceof Entity entity) {
            return entity.getPersistentDataContainer();
        } else if (inv.getHolder() instanceof DoubleChest dbc) {
            return ((Chest) dbc.getLeftSide()).getPersistentDataContainer();
        }
        return null;
    }

    public boolean isLockedBlock(String p, Block b) {
        if (b == null || !(b.getState() instanceof TileState tileState)) {
            return false;
        }

        if (b.getState() instanceof Container c && c.getInventory().getHolder() instanceof DoubleChest dbc)
            return isLocked(p, ((Chest) dbc.getLeftSide()).getPersistentDataContainer())
                    || isLocked(p, ((Chest) dbc.getRightSide()).getPersistentDataContainer());

        return isLocked(p, tileState.getPersistentDataContainer());
    }

    public boolean isLockedEntity(String p, Entity e) {
        if (e == null)
            return false;
        return isLocked(p, e.getPersistentDataContainer());
    }

    public boolean isLocked(String player, PersistentDataContainer data) {
        if (data == null || !data.has(Utils.ownersKey))
            return false;

        List<String> owners = List.of(data.get(Utils.ownersKey, PersistentDataType.STRING).split(", "));
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
