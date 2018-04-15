package com.ruinscraft.chip.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.ruinscraft.chip.ChipPlugin;

public class PlayerListener implements Listener {

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		final Player player = event.getPlayer();
		final ItemStack itemStack = event.getItem();
		
		if (itemStack == null) {
			return;
		}
		
		if (itemStack.getType() == Material.AIR) {
			return;
		}
		
		if (player.hasPermission(ChipPlugin.PERMISSION_BYPASS)) {
			return;
		}
		
		if (ChipPlugin.hasModifications(itemStack)) {
			event.setCancelled(true);
		}
		
		if (event.isCancelled()) {
			player.updateInventory();
		}
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		final ItemStack itemStack = event.getCurrentItem();
		
		if (itemStack == null) {
			return;
		}
		
		if (itemStack.getType() == Material.AIR) {
			return;
		}
		
		if (event.getWhoClicked().hasPermission(ChipPlugin.PERMISSION_BYPASS)) {
			return;
		}
		
		if (ChipPlugin.hasModifications(itemStack)) {
			ChipPlugin.fixItemStack(itemStack);
		}
	}
	
	@EventHandler
	public void onDropItem(PlayerDropItemEvent event) {
		final ItemStack itemStack = event.getItemDrop().getItemStack();
		
		if (itemStack == null) {
			return;
		}
		
		if (itemStack.getType() == Material.AIR) {
			return;
		}
		
		if (event.getPlayer().hasPermission(ChipPlugin.PERMISSION_BYPASS)) {
			return;
		}
		
		if (ChipPlugin.hasModifications(itemStack)) {
			ChipPlugin.fixItemStack(itemStack);
		}
	}
	
}
