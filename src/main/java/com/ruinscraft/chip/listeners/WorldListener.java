package com.ruinscraft.chip.listeners;

import java.util.logging.Level;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;

import com.ruinscraft.chip.ChipPlugin;

public class WorldListener implements Listener {

	@EventHandler
	public void onBlockPhysics(BlockPhysicsEvent event) {
		Block block = event.getBlock();
		
		if (ChipPlugin.getInstance().waterFlow) {
			if (block.getType().name().toLowerCase().contains("water")) {
				event.setCancelled(true);
			}
		}
		
		if (ChipPlugin.getInstance().lavaFlow) {
			if (block.getType().name().toLowerCase().contains("lava")) {
				event.setCancelled(true);
			}
		}
		
		if (ChipPlugin.getInstance().tntUpdate) {
			if (block.getType().name().toLowerCase().contains("tnt")) {
				event.setCancelled(true);
			}
		}
		
		if (ChipPlugin.getInstance().spongeUpdate) {
			if (block.getType().name().toLowerCase().contains("sponge")) {
				event.setCancelled(true);
			}
		}
		
		if (event.isCancelled() && ChipPlugin.getInstance().notifyConsoleWhenCancelled) {
			ChipPlugin.getInstance().getLogger().log(Level.INFO, "Canceled blockphysics: " + block.getType().name() + " -> "+ block.getX() + "," + block.getY() + "," + block.getZ());
		}
	}

}
