package com.ruinscraft.chip.listeners;

import java.util.Optional;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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
			ChipPlugin.cleanInventory(Optional.of(player.getName()), player.getInventory());
		}
	}
	
}
