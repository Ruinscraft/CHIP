package com.ruinscraft.chip.tasks;

import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.ruinscraft.chip.ChipPlugin;

public class CleanInventoriesTask implements Runnable {

	@Override
	public void run() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (player == null) {
				continue;
			}
			
			ChipPlugin.cleanInventory(Optional.of(player.getName()), player.getInventory());
			
			// use player.getName() so that if they have permission, their enderchest won't get cleaned
			ChipPlugin.cleanInventory(Optional.of(player.getName()), player.getEnderChest());
		}
	}
	
}
