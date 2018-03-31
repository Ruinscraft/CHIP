package com.ruinscraft.chip.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;

public class WorldListener implements Listener {

	@EventHandler
	public void onBlockPhysics(BlockPhysicsEvent event) {
		
		Block block = event.getBlock();
		
		if (block.getType() == Material.SPONGE) {
			event.setCancelled(true);
		}
		
		if (block.getType() == Material.TNT) {
			event.setCancelled(true);
		}
		
		if (block.getType() == Material.WATER) {
			if (block.getRelative(BlockFace.DOWN).getType() == Material.SPONGE) {
				event.setCancelled(true);
			}
			
			if (block.getRelative(BlockFace.UP).getType() == Material.SPONGE) {
				event.setCancelled(true);
			}
			
			if (block.getRelative(BlockFace.EAST).getType() == Material.SPONGE) {
				event.setCancelled(true);
			}
			
			if (block.getRelative(BlockFace.WEST).getType() == Material.SPONGE) {
				event.setCancelled(true);
			}
			
			if (block.getRelative(BlockFace.NORTH).getType() == Material.SPONGE) {
				event.setCancelled(true);
			}
			
			if (block.getRelative(BlockFace.SOUTH).getType() == Material.SPONGE) {
				event.setCancelled(true);
			}
		}
		
	}
	
}
