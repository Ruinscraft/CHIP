package com.ruinscraft.chip.listeners;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;

public class WorldListener implements Listener {

	@EventHandler
	public void onBlockPhysics(BlockPhysicsEvent event) {
		Block block = event.getBlock();
		
		if (block.getType().name().toLowerCase().contains("water")) {
			event.setCancelled(true);
		}
		
		if (block.getType().name().toLowerCase().contains("lava")) {
			event.setCancelled(true);
		}
		
		if (block.getType().name().toLowerCase().contains("tnt")) {
			event.setCancelled(true);
		}
		
		if (block.getType().name().toLowerCase().contains("sponge")) {
			event.setCancelled(true);
		}

		if (event.isCancelled()) {
			System.out.println("canceled blockphysics: " + block.getType().name() + " -> "+ block.getX() + "," + block.getY() + "," + block.getZ());
		}
	}

}
