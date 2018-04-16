package com.ruinscraft.chip.listeners;

import java.util.Optional;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import com.ruinscraft.chip.ChipPlugin;

public class WorldListener implements Listener {
	
	@EventHandler
	public void onBlockDispense(BlockDispenseEvent event) {
		final ItemStack itemStack = event.getItem();
		
		if (itemStack == null) {
			return;
		}
		
		int modCount = ChipPlugin.fixItemStack(itemStack);
		
		if (modCount > 0) {
			final Block block = event.getBlock();
			
			if (block == null) {
				return;
			}
			
			if (block.getState() instanceof InventoryHolder) {
				final InventoryHolder inventoryHolder = (InventoryHolder) block.getState();

				ChipPlugin.cleanInventory(Optional.of(block.getType().name() + " (" + block.getX() + ", " + block.getY() + ", " + block.getZ() + ")"), inventoryHolder.getInventory());
			}
		}
	}

}
