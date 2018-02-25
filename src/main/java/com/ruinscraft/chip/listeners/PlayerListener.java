package com.ruinscraft.chip.listeners;

import java.util.Optional;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.ruinscraft.chip.CHIPPlugin;
import com.ruinscraft.chip.CHIPUtil;
import com.ruinscraft.chip.InvalidAttributeException;

public class PlayerListener implements Listener {

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		final Player player = event.getPlayer();
		final ItemStack itemStack = event.getItem();
		
		if (player.hasPermission(CHIPPlugin.PERMISSION_BYPASS)) {
			return;
		}
		
		if (itemStack == null) {
			return;
		}
		
		if (itemStack.getType() == Material.AIR) {
			return;
		}
		
		try {
			CHIPUtil.checkItem(itemStack);
		} catch (InvalidAttributeException e) {
			event.setCancelled(true);
			CHIPUtil.cleanInventory(Optional.of(player.getName()), player.getInventory());
		}
	}
	
}
