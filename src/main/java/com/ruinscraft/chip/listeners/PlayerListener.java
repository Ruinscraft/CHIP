package com.ruinscraft.chip.listeners;

import java.util.Optional;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.ruinscraft.chip.CHIPPlugin;
import com.ruinscraft.chip.InvalidAttributeException;

public class PlayerListener implements Listener {

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		final Player player = event.getPlayer();
		final ItemStack itemStack = event.getItem();
		
		if (player.hasPermission(CHIPPlugin.getInstance().PERMISSION_BYPASS)) {
			return;
		}
		
		if (itemStack == null) {
			return;
		}
		
		if (itemStack.getType() == Material.AIR) {
			return;
		}
		
		try {
			CHIPPlugin.getInstance().getUtil().checkItem(itemStack);
		} catch (InvalidAttributeException e) {
			event.setCancelled(true);
			CHIPPlugin.getInstance().getUtil().cleanInventory(Optional.of(player.getName()), player.getInventory());
		}
	}
	
}
