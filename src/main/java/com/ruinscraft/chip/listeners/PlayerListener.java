package com.ruinscraft.chip.listeners;

import java.util.Optional;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import com.ruinscraft.chip.ChipPlugin;
import com.ruinscraft.chip.ChipUtil;

public class PlayerListener implements Listener {

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		final Player player = event.getPlayer();
		
		final ItemStack itemStack = event.getItem();
		
		ChipUtil.fix(player.getLocation().getWorld().getName(), itemStack, Optional.of(player.getName()), Optional.of(ChipUtil.getLocationString(player.getLocation())), Optional.of(player.getInventory()));
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		final ItemStack itemStack = event.getCurrentItem();
		
		ChipUtil.fix(event.getWhoClicked().getLocation().getWorld().getName(), itemStack, Optional.of(event.getWhoClicked().getName()), Optional.of(ChipUtil.getLocationString(event.getWhoClicked().getLocation())), Optional.of(event.getWhoClicked().getInventory()));
	}
	
	@EventHandler
	public void onDropItem(PlayerDropItemEvent event) {
		final Player player = event.getPlayer();
		
		final ItemStack itemStack = event.getItemDrop().getItemStack();
		
		ChipUtil.fix(player.getLocation().getWorld().getName(), itemStack, Optional.of(player.getName()), Optional.of(ChipUtil.getLocationString(player.getLocation())), Optional.of(player.getInventory()));
	}
	
	@EventHandler
	public void onInventoryOpen(InventoryOpenEvent event) {
		ChipUtil.fixInventory(event.getPlayer().getLocation().getWorld().getName(), event.getInventory(), Optional.of(event.getPlayer().getName()));
	}
	
	@EventHandler
	public void onPlayerEditBook(PlayerEditBookEvent event) {
		if (!ChipPlugin.getInstance().preventBookForgery) {
			return;
		}
		
		final Player player = event.getPlayer();
		
		final String prevAuthor = event.getPreviousBookMeta().getAuthor();
		final String newAuthor = event.getNewBookMeta().getAuthor();
		
		boolean setNewMeta = false;
		
		if (event.isSigning()) {
			if (!newAuthor.equals(player.getName())) {
				setNewMeta = true;
			}
		}
		
		if (!prevAuthor.equals(newAuthor)) {
			setNewMeta = true;
		}
		
		if (setNewMeta) {
			BookMeta newBookMeta = event.getNewBookMeta().clone();
			
			newBookMeta.setAuthor(player.getName());
			
			event.setNewBookMeta(newBookMeta);
		}
	}
	
}
