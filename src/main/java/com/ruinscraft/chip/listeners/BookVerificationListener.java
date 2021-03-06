package com.ruinscraft.chip.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import com.ruinscraft.chip.ChipPlugin;
import com.ruinscraft.chip.util.ChipUtil;
import com.ruinscraft.chip.BookSig;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class BookVerificationListener implements Listener {

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

			// only check the book if it was right clicked
			if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
				checkBook = true;
			}

			if (checkBook) {
				BookMeta bookMeta = (BookMeta) itemStack.getItemMeta();

				if (!ChipUtil.bookHasSig(bookMeta, itemStack) || ChipUtil.bookKnownForged(bookMeta, itemStack)) {
					if (ChipPlugin.getInstance().alertIfForgedActionBar) {
						String warning = ChatColor.UNDERLINE + ChatColor.BOLD.toString() + "THIS BOOK IS NOT VERIFIED. THE AUTHOR/CONTENT MAY BE FAKE.";
						player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(warning));
					}

					if (ChipPlugin.getInstance().alertIfForgedChat) {
						String warning = ChatColor.UNDERLINE + ChatColor.BOLD.toString() + ChatColor.RED + "THIS BOOK IS NOT VERIFIED. THE AUTHOR/CONTENT MAY BE FAKE.";
						String command = ChatColor.UNDERLINE + ChatColor.BOLD.toString() + ChatColor.RED + "Have the author which appears on this book run /verifybook";
						player.sendMessage(warning);
						player.sendMessage(command);
					}
				} else {
					BookSig bookSig = ChipUtil.getBookSig(bookMeta, itemStack);

					long time = bookSig.getDatestamp();

					if (ChipPlugin.getInstance().alertIfVerifiedChat) {
						String message = ChatColor.GREEN + "This book's author and content have been verified. It was signed on " + ChipUtil.getDateStringFromMillis(time) + ".";
						player.sendMessage(message);
					}

					if (ChipPlugin.getInstance().alertIfVerifiedActionBar) {
						String message = ChatColor.GREEN + "This book's author and content have been verified.";
						player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(message));
					}
				}
			}
		}
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		final ItemStack itemStack = event.getCurrentItem();

		if (itemStack == null || itemStack.getType() == Material.AIR) {
			return;
		}

		if (ChipPlugin.getInstance().preventBookForgery && ChipPlugin.getInstance().preventDistributionOfNonVerifiedBooks) {
			InventoryType inventoryType = event.getInventory().getType();

			boolean canMove = false;

			if (inventoryType == InventoryType.CRAFTING || inventoryType == InventoryType.PLAYER) {
				canMove = true;
			}

			if (itemStack.getType() == Material.WRITTEN_BOOK) {
				BookMeta bookMeta = (BookMeta) itemStack.getItemMeta();

				if (ChipUtil.bookKnownForged(bookMeta, itemStack) || !ChipUtil.bookHasSig(bookMeta, itemStack)) {
					if (!canMove) {
						// try to cast and send warning
						try {
							Player player = (Player) event.getWhoClicked();

							player.sendMessage(ChatColor.RED + "You are not allowed to store unverified books. Type /verifybook if you own this book.");
							// not a player??
						} catch (Exception e) {}

						if (event.getCursor() != null) {
							event.setCancelled(true);
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void onDropItem(PlayerDropItemEvent event) {
		final Player player = event.getPlayer();

		final ItemStack itemStack = event.getItemDrop().getItemStack();

		if (ChipPlugin.getInstance().preventBookForgery && ChipPlugin.getInstance().preventDistributionOfNonVerifiedBooks) {
			if (itemStack.getType() == Material.WRITTEN_BOOK) {
				BookMeta bookMeta = (BookMeta) itemStack.getItemMeta();

				if (ChipUtil.bookKnownForged(bookMeta, itemStack) || !ChipUtil.bookHasSig(bookMeta, itemStack)) {
					player.sendMessage(ChatColor.RED + "You are not allowed to drop unverified books. Type /verifybook if you own this book.");
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void onPlayerEditBook(PlayerEditBookEvent event) {
		if (!ChipPlugin.getInstance().preventBookForgery) {
			return;
		}

		BookMeta bookMeta = event.getNewBookMeta();

		if (event.isSigning()) {
			BookMeta newBookMeta = ChipUtil.getBookMetaWithBookSig(bookMeta, BookSig.create(bookMeta));
			
			event.setNewBookMeta(newBookMeta);
		}
	}

}
