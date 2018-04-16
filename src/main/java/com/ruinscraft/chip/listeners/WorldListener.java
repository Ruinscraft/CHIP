package com.ruinscraft.chip.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.inventory.ItemStack;

import com.ruinscraft.chip.ChipPlugin;

public class WorldListener implements Listener {
	
	@EventHandler
	public void onBlockDispense(BlockDispenseEvent event) {
		final ItemStack itemStack = event.getItem();
		
		if (itemStack == null) {
			return;
		}
		
		ChipPlugin.fixItemStack(itemStack);
	}

}
