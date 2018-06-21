package com.ruinscraft.chip.listeners;

import java.util.Optional;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import com.ruinscraft.chip.ChipPlugin;
import com.ruinscraft.chip.ChipUtil;
import com.ruinscraft.chip.SignedBook;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class PlayerListener implements Listener {

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		final Player player = event.getPlayer();

		final ItemStack itemStack = event.getItem();

		if (itemStack == null || itemStack.getType() == Material.AIR) {
			return;
		}

		if (ChipPlugin.getInstance().preventBookForgery && itemStack.getType() == Material.WRITTEN_BOOK) {
			boolean checkBook = false;

			Action action = event.getAction();

			if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
				checkBook = true;
			}

			if (checkBook) {
				BookMeta bookMeta = (BookMeta) itemStack.getItemMeta();

				if (!ChipUtil.bookIsVerified(bookMeta)) {
					if (ChipPlugin.getInstance().alertIfForgedActionBar) {
						String warning = ChatColor.UNDERLINE + ChatColor.BOLD.toString() + "THIS BOOK IS NOT VERIFIED. THE AUTHOR MAY BE FAKE.";
						player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(warning));
					}

					if (ChipPlugin.getInstance().alertIfForgedChat) {
						String warning = ChatColor.UNDERLINE + ChatColor.BOLD.toString() + ChatColor.RED + "THIS BOOK IS NOT VERIFIED. THE AUTHOR MAY BE FAKE.";
						String command = ChatColor.UNDERLINE + ChatColor.BOLD.toString() + ChatColor.RED + "Have the author which appears on this book run /verifybook";
						player.sendMessage(warning);
						player.sendMessage(command);
					}
				} else {
					SignedBook signedBook = ChipUtil.getSignedBook(bookMeta);

					long time = signedBook.getDatestamp();

					if (ChipPlugin.getInstance().alertIfVerifiedChat) {
						String message = ChatColor.GREEN + "The author of this book has been verified. It was signed on " + ChipUtil.getDateStringFromMillis(time) + ".";
						player.sendMessage(message);
					}

					if (ChipPlugin.getInstance().alertIfVerifiedActionBar) {
						String message = ChatColor.GREEN + "The author of this book has been verified.";
						player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
					}
				}
			}
		}

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

		if (itemStack == null) {
			return;
		}

		if (itemStack.getType() == Material.AIR) {
			return;
		}

		if (ChipPlugin.getInstance().preventBookForgery && itemStack.getType() == Material.WRITTEN_BOOK) {
			BookMeta bookMeta = (BookMeta) itemStack.getItemMeta();
			
			if (!ChipUtil.bookIsVerified(bookMeta) && !ChipUtil.bookIsLegacy(bookMeta)) {
				event.setCancelled(true);
				player.sendMessage(ChatColor.RED + "You are not allowed to drop books with changed authors.");
			}
		}

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

		if (event.isSigning()) {
			event.setNewBookMeta(ChipUtil.addAuthorToBookLore(event.getNewBookMeta(), player.getName()));
		}
	}

}
