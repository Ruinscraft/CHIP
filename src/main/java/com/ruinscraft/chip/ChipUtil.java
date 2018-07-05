package com.ruinscraft.chip;

import java.util.*;
import java.util.stream.Collectors;
import java.text.SimpleDateFormat;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class ChipUtil {

	private static final ChipPlugin chip = ChipPlugin.getInstance();
	private static final Gson gson = new Gson();

	private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("EEEE, MMMM d, yyyy");

	public static String getDateStringFromMillis(long millis) {
		Date date = new Date(millis);

		return dateFormatter.format(date);
	}

	public static BookMeta getBookMetaWithBookSig(BookMeta bookMeta, BookSig bookSig) {
		try {
			Crypto crypto = chip.getCrypto();
			String bookSigJson = gson.toJson(bookSig);
			String encrypted = crypto.encrypt(bookSigJson);
			String loreLine = LoreUtil.encodeStringForLore(encrypted);
			BookMeta newBookMeta = bookMeta.clone();

			newBookMeta.setLore(Arrays.asList(loreLine));

			return newBookMeta;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return bookMeta;
	}

	public static BookSig getBookSig(BookMeta bookMeta, ItemStack itemStack) {
		if (!bookMeta.hasLore()) {
			return null;
		}

		BookSig bookSig = null;

		for (String line : bookMeta.getLore()) {
			try {
				String decodedLine = LoreUtil.decodeStringForLore(line);
				String decryptedLine = chip.getCrypto().decrypt(decodedLine);

				bookSig = gson.fromJson(decryptedLine, BookSig.class);
				// cannot decode json, isnt a booksig
			} catch (Exception e) {}
		}

		if (bookSig != null && bookSig.getContentSum() == null) {
			bookSig.setContentSum(getMd5HashOfBookContent(bookMeta));

			bookMeta = getBookMetaWithBookSig(bookMeta, bookSig);

			try {
				itemStack.setItemMeta(bookMeta);
				// is not a book?
			} catch (Exception e) {}
		}

		return bookSig;
	}

	public static boolean bookKnownForged(BookMeta bookMeta, ItemStack itemStack) {
		BookSig bookSig = getBookSig(bookMeta, itemStack);

		// books without a booksig cant be known to be frauds, they could just be old books
		if (bookSig == null) {
			return false;
		}

		// if author was changed
		if (!bookMeta.getAuthor().equals(bookSig.getOriginalAuthor())) {
			return true;
		}

		// if book content was changed
		if (bookSig.getContentSum() != null && !getMd5HashOfBookContent(bookMeta).equals(bookSig.getContentSum())) {
			return true;
		}

		return false;
	}
	
	public static boolean bookHasSig(BookMeta bookMeta, ItemStack itemStack) {
		BookSig bookSig = getBookSig(bookMeta, itemStack);

		return bookSig != null;
	}

	public static String getMd5HashOfBookContent(BookMeta bookMeta) {
		String jsonBookContent = gson.toJson(bookMeta.getPages(), new TypeToken<List<String>>(){}.getType());
		HashCode hash = Hashing.md5().hashBytes(jsonBookContent.getBytes());

		return hash.toString();
	}

	public static Set<Modification> check(Object o) {
		return chip.getCheckerCache().getUnchecked(o);
	}
	
	public static Set<Modification> check(String world, Object o) {
		if (chip.useWorldWhitelist && !chip.whitelistedWorlds.contains(world)) {
			return new HashSet<>();
		}

		return chip.getCheckerCache().getUnchecked(o);
	}

	public static List<String> getWords(Set<Modification> modifications) {
		return modifications.stream().map(Modification::getPretty).collect(Collectors.toList());
	}

	public static boolean hasModificationsForWorld(String world, Object o) {
		return !check(world, o).isEmpty();
	}
	
	public static boolean hasModificationsAtAll(Object o) {
		return !check(o).isEmpty();
	}

	public static String getLocationString(Location location) {
		return location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ();
	}

	public static void fix(String world, Object o, Optional<String> parent, Optional<String> location, Optional<Inventory> parentInventory) {
		if (o instanceof ItemStack) {
			// check if player is allowed to have modified items
			try {
				Player player = Bukkit.getPlayer(parent.get());

				if (player.hasPermission(ChipPlugin.PERMISSION_BYPASS)) {
					return;
				}

				if (chip.opsBypassChecks && player.isOp()) {
					return;
				}
			} catch (Exception e) {
				// parent is not player, possibly a block such as a chest
			}

			ItemStack itemStack = (ItemStack) o;
			Set<Modification> modifications = check(world, itemStack);

			if (!modifications.isEmpty()) {
				if (chip.removeItem) {
					if (parentInventory.isPresent()) {
						parentInventory.get().removeItem(itemStack);
					}
				}

				chip.getItemStackFixer().fix(itemStack, modifications);

				notify(Optional.of(itemStack.getType().name()), parent, location, modifications);
			}
		}

		else if (o instanceof Entity) {
			Entity entity = (Entity) o;

			if (entity instanceof Item) {
				final Item item = (Item) entity;

				fix(world, item.getItemStack(), parent, location, parentInventory);

				return;
			}

			Set<Modification> modifications = check(world, entity);

			if (!modifications.isEmpty()) {

				if (chip.removeEntity) {
					entity.remove();
				} else {
					chip.getEntityFixer().fix(entity, modifications);
				}

				notify(Optional.of(entity.getType().name()), 
						parent, 
						Optional.of(getLocationString(entity.getLocation())), 
						modifications);
			}
		}
	}

	public static void fixInventory(String world, Inventory inventory, Optional<String> parent) {
		inventory.forEach(itemStack -> fix(world, itemStack, parent, Optional.empty(), Optional.of(inventory)));
	}

	public static void notify(Optional<String> fixedObject, Optional<String> parent, Optional<String> location, Set<Modification> modifications) {
		String raw = fixedObject.orElse("?") + " was modified (loc: " + location.orElse("?") + ")" + " Owner: " + parent.orElse("?");

		if (chip.chatNotifications) {
			TextComponent message = new TextComponent(raw);

			message.setColor(ChipPlugin.COLOR_BASE);
			message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(String.join(", ", getWords(modifications))).create()));

			Bukkit.getOnlinePlayers().forEach(player -> {
				if (player.hasPermission(ChipPlugin.PERMISSION_NOTIFY)) player.spigot().sendMessage(message);
			});
		}

		if (chip.consoleNotifications) {
			chip.getLogger().info(raw);
		}
	}

}
