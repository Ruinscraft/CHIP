package com.ruinscraft.chip;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import com.google.common.base.Preconditions;

import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class ChipUtil {

	private static final ChipPlugin chip = ChipPlugin.getInstance();

	// https://www.spigotmc.org/threads/how-to-hide-item-lore-how-to-bind-data-to-itemstack.196008/
	public static String encodeString(String msg) {
		StringBuilder output = new StringBuilder();

        byte[] bytes = msg.getBytes(StandardCharsets.UTF_8);
        String hex = Hex.encodeHexString(bytes);

        for (char c : hex.toCharArray()) {
            output.append(ChatColor.COLOR_CHAR).append(c);
        }

        return output.toString();
	}
	
	// https://www.spigotmc.org/threads/how-to-hide-item-lore-how-to-bind-data-to-itemstack.196008/
	public static String decodeString(String msg) {
        if (msg.isEmpty()) {
            return msg;
        }

        char[] chars = msg.toCharArray();

        char[] hexChars = new char[chars.length / 2];

        IntStream.range(0, chars.length)
                .filter(value -> value % 2 != 0)
                .forEach(value -> hexChars[value / 2] = chars[value]);

        try {
            return new String(Hex.decodeHex(hexChars), StandardCharsets.UTF_8);
        } catch (DecoderException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Couldn't decode text", e);
        }
	}
	
	public static BookMeta addAuthorToBookLore(BookMeta bookMeta, String newAuthor) {
		Preconditions.checkNotNull(bookMeta, "bookMeta cannot be null");
		Preconditions.checkNotNull(newAuthor, "newAuthor cannot be null");
		
		BookMeta newMeta = bookMeta.clone();
		
		newMeta.setLore(Arrays.asList(ChipUtil.encodeString("original_author:" + newAuthor)));
		
		return newMeta;
	}
	
	public static Set<Modification> check(String world, Object o) {
		Preconditions.checkNotNull(world, "world cannot be null");
		Preconditions.checkNotNull(o, "object to check cannot be null");

		if (chip.useWorldWhitelist && !chip.whitelistedWorlds.contains(world)) {
			return new HashSet<>();
		}

		return chip.getCheckerCache().getUnchecked(o);
	}

	public static List<String> getWords(Set<Modification> modifications) {
		return modifications.stream().map(Modification::getPretty).collect(Collectors.toList());
	}

	public static boolean hasModifications(String world, Object o) {
		return !check(world, o).isEmpty();
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
					boolean containsHardModifications = false;
					
					for (Modification modification : modifications) {
						if (!modification.isSoft()) {
							containsHardModifications = true;
						} else {
							modifications.remove(modification);
						}
					}
					
					if (parentInventory.isPresent() && containsHardModifications) {
						parentInventory.get().remove(itemStack);
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
