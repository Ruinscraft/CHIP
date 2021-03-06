package com.ruinscraft.chip.util;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import com.google.common.collect.Sets;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ruinscraft.chip.BookSig;
import com.ruinscraft.chip.ChipPlugin;
import com.ruinscraft.chip.Modification;

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
	
	public static String getFormattedLocation(Location location) {
		return location.getWorld().getName() + ": " + (int) location.getX() + ", " + (int) location.getY() + ", " + (int) location.getZ() + "";
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

	public static Set<Modification> check(String world, Object checkable) {
		if (chip.useWorldWhitelist && !chip.whitelistedWorlds.contains(world)) {
			return Sets.newHashSet();
		}

		return chip.getCheckerCache().getUnchecked(checkable);
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

	public static void fix(Object fixable, Object parent) {
		if (fixable == null) {
			return;
		}
		
		if (parent instanceof Player) {
			if (((Player) parent).hasPermission(chip.PERMISSION_BYPASS)) return;
			if (((Player) parent).isOp() && chip.opsBypassChecks) return;
		}

		Set<Modification> modifications = null;
		
		if (fixable instanceof Entity) {
			// entities cant be cached (for now)
			modifications = chip.getEntityChecker().getModifications((Entity) fixable);
		} else {
			modifications = check(fixable);
		}
		
		if (modifications.isEmpty()) {
			return;
		}
		
		Optional<String> description = Optional.empty();
		Optional<String> parentName = Optional.empty();
		Optional<Location> location = Optional.empty();
		Optional<Inventory> inventory = Optional.empty();

		try {
			// check fixable, might be an entity or itemstack
			for (Method method : fixable.getClass().getMethods()) {
				switch (method.getName()) {
				case "getName": {
					description = Optional.ofNullable((String) method.invoke(fixable));
					break;
				}
				case "getType": {
					description = Optional.ofNullable((String) method.invoke(fixable).toString());
					break;
				}
				case "getLocation": {
					if (method.getParameterCount() > 0) break;
					location = Optional.ofNullable((Location) method.invoke(fixable));
					break;
				}
				case "getInventory": {
					inventory = Optional.ofNullable((Inventory) method.invoke(fixable));
					break;
				}
				}
			}

			// check parent, might be a player or block
			for (Method method : parent.getClass().getMethods()) {
				switch (method.getName()) {
				case "getLocation": {
					if (method.getParameterCount() > 0) break;
					location = Optional.ofNullable((Location) method.invoke(parent));
					break;
				}
				case "getName": {
					parentName = Optional.ofNullable((String) method.invoke(parent));
					break;
				}
				case "getInventory": {
					inventory = Optional.ofNullable((Inventory) method.invoke(parent));
					break;
				}
				case "getType": {
					parentName = Optional.ofNullable(method.invoke(parent).toString());
					break;
				}
				}
			}
		} catch (Exception e) {}
		
		// notify
		notify(description, parentName, Optional.ofNullable(getFormattedLocation(location.get())), modifications);
		
		// handle ItemStack
		if (fixable instanceof ItemStack) {
			if (inventory.isPresent()) {
				if (chip.removeItem) {
					inventory.get().remove((ItemStack) fixable);
					
					return;
				}
				
				for (ItemStack itemStack : inventory.get().getContents()) {
					if (itemStack == null || itemStack.getType() == Material.AIR) {
						continue;
					}
					
					chip.getItemStackFixer().fix(itemStack, check(itemStack));
				}
			}
			
			chip.getItemStackFixer().fix((ItemStack) fixable, modifications);
		}
		
		// handle Entity
		if (fixable instanceof Entity) {
			if (chip.removeEntity) {
				try {
					fixable.getClass().getMethod("remove").invoke(fixable);
				} catch (Exception e) {
					chip.getLogger().info("Could not remove entity. " + fixable.getClass().getName());
				}
				
				return;
			}
			
			chip.getEntityFixer().fix((Entity) fixable, modifications);
		}
	}

	public static void notify(Optional<String> fixedObject, Optional<String> parentName, Optional<String> location, Set<Modification> modifications) {
		String locRaw = location.isPresent() ? location.get().toString() : "?";
		String raw = fixedObject.orElse("?") + " was modified (" + locRaw + ")" + " Parent: " + parentName.orElse("?");

		if (chip.chatNotifications) {
			TextComponent message = new TextComponent(raw);

			message.setColor(chip.COLOR_BASE);
			message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(String.join(", ", getWords(modifications))).create()));

			Bukkit.getOnlinePlayers().forEach(player -> {
				if (player.hasPermission(chip.PERMISSION_NOTIFY)) player.spigot().sendMessage(message);
			});
		}

		if (chip.consoleNotifications) {
			chip.getLogger().info(raw);
		}
	}

}
