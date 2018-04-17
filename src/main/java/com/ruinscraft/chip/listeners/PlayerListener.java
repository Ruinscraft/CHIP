package com.ruinscraft.chip.listeners;

import java.util.Optional;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.ruinscraft.chip.ChipUtil;

public class PlayerListener implements Listener {

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		final Player player = event.getPlayer();
		
		final ItemStack itemStack = event.getItem();
		
		ChipUtil.fix(itemStack, Optional.of(player.getName()), Optional.of(ChipUtil.getLocationString(player.getLocation())), Optional.of(player.getInventory()));
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		final ItemStack itemStack = event.getCurrentItem();
		
		ChipUtil.fix(itemStack, Optional.of(event.getWhoClicked().getName()), Optional.of(ChipUtil.getLocationString(event.getWhoClicked().getLocation())), Optional.of(event.getWhoClicked().getInventory()));
	}
	
	@EventHandler
	public void onDropItem(PlayerDropItemEvent event) {
		final Player player = event.getPlayer();
		
		final ItemStack itemStack = event.getItemDrop().getItemStack();
		
		ChipUtil.fix(itemStack, Optional.of(player.getName()), Optional.of(ChipUtil.getLocationString(player.getLocation())), Optional.of(player.getInventory()));
	}
	
	@EventHandler
	public void onInventoryOpen(InventoryOpenEvent event) {
		ChipUtil.fixInventory(event.getInventory(), Optional.of(event.getPlayer().getName()));
	}
	
}
