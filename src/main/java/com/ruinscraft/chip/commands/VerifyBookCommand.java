package com.ruinscraft.chip.commands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import com.ruinscraft.chip.BookSig;
import com.ruinscraft.chip.ChipPlugin;
import com.ruinscraft.chip.util.ChipUtil;

public class VerifyBookCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			return false;
		}
		
		Player player = (Player) sender;
		
		if (!ChipPlugin.getInstance().preventBookForgery) {
			player.sendMessage(ChatColor.RED + "This feature is not enabled.");
			
			return true;
		}

		// use getItemInHand for legacy support
		ItemStack itemInHand = player.getItemInHand();
		
		if (itemInHand.getType() != Material.WRITTEN_BOOK) {
			player.sendMessage(ChatColor.RED + "You are not holding a written book.");
			
			return true;
		}
		
		if (itemInHand.getItemMeta() == null) {
			player.sendMessage(ChatColor.RED + "Failed to get item information.");
			
			return true;
		}
		
		if (!(itemInHand.getItemMeta() instanceof BookMeta)) {
			player.sendMessage(ChatColor.RED + "Failed to get book information.");
			
			return true;
		}
		
		BookMeta bookMeta = (BookMeta) itemInHand.getItemMeta();
		
		String newAuthor;
		
		if (args.length > 0) {
			if (!player.hasPermission("chip.command.verifybook.override")) {
				player.sendMessage(ChatColor.RED + "You do not have permission to override book authors.");
				
				return true;
			}
			
			newAuthor = args[0];
		} else {
			String author = bookMeta.getAuthor();
			
			if (!player.getName().equalsIgnoreCase(author)) {
				player.sendMessage(ChatColor.RED + "You are not the author of this book (if you changed your username, have a staff member override it).");
				
				return true;
			}
			
			newAuthor = player.getName();
		}
		
		BookMeta newBookMeta = ChipUtil.getBookMetaWithBookSig(bookMeta, BookSig.create(bookMeta, newAuthor));

		itemInHand.setItemMeta(newBookMeta);
		
		player.sendMessage(ChatColor.GREEN + "Book verified with username: " + newAuthor);
		
		return true;
	}
	
}
