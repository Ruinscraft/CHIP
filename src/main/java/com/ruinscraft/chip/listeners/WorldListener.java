package com.ruinscraft.chip.listeners;

import java.util.Optional;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import com.ruinscraft.chip.ChipUtil;

public class WorldListener implements Listener {

	@EventHandler
	public void onBlockDispense(BlockDispenseEvent event) {
		final ItemStack itemStack = event.getItem();

		final Block block = event.getBlock();

		if (ChipUtil.hasModifications(block.getLocation().getWorld().getName(), itemStack)) {
			if (block.getState() instanceof InventoryHolder) {
				final InventoryHolder inventoryHolder = (InventoryHolder) block.getState();

				ChipUtil.fixInventory(block.getLocation().getWorld().getName(), inventoryHolder.getInventory(), Optional.of(block.getType().name()));
			}
		}
	}

	@EventHandler
	public void onEntitySpawn(EntitySpawnEvent event) {
		final Entity entity = event.getEntity();

		ChipUtil.fix(entity.getLocation().getWorld().getName(), entity, Optional.empty(), Optional.of(ChipUtil.getLocationString(entity.getLocation())), Optional.empty());
	}

}
