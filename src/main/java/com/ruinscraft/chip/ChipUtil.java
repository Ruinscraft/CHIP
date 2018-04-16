package com.ruinscraft.chip;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

// TODO: refactor
public class ChipUtil {

	private static final ChipPlugin chip = ChipPlugin.getInstance();

	public static Set<Modification> getModifications(Object o) {
		return chip.getCheckerCache().getUnchecked(o);
	}

	public static List<String> getModificationsWords(Object o) {
		return getModifications(o).stream().map(Modification::getPretty).collect(Collectors.toList());
	}

	public static boolean hasModifications(Object o) {
		return !getModifications(o).isEmpty();
	}

	public static void fixItemStack(ItemStack itemStack) {
		chip.getItemStackFixer().fix(itemStack);
	}

	public static void fixEntity(Entity entity) {
		chip.getEntityFixer().fix(entity);
	}

	public static void cleanInventory(Optional<String> description, Inventory inventory) {
		try {
			Player player = Bukkit.getPlayer(description.get());

			if (player.hasPermission(ChipPlugin.PERMISSION_BYPASS)) {
				return;
			}

			if (chip.opsBypassChecks && player.isOp()) {
				return;
			}

			// this will clean the user's enderchest if they don't have bypass permission
			// it won't run again because "<player>'s Enderchest" is not a player
			cleanInventory(Optional.of(player.getName() + "'s Enderchest"), player.getEnderChest());
		} catch (Exception e) {
			// do nothing
		}

		for (ItemStack itemStack : inventory.getContents()) {
			if (itemStack == null) {
				continue;
			}

			if (itemStack.getType() == Material.AIR) {
				continue;
			}

			if (hasModifications(itemStack)) {
				TextComponent message = new TextComponent(description.orElse("?") + " had modified " + itemStack.getType().name() + " (hover for info)");

				message.setColor(ChipPlugin.COLOR_BASE);

				message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(String.join(", ", getModificationsWords(itemStack))).create()));

				notify(message);

				if (chip.removeItem) {
					inventory.remove(itemStack);
				} else {
					chip.getItemStackFixer().fix(itemStack);
				}
			}
		}
	}

	public static void cleanEntity(Optional<String> description, Entity entity) {
		if (entity == null) {
			return;
		}

		if (hasModifications(entity)) {
			TextComponent message = new TextComponent(entity.getType().name() + " had modifications (hover for info)");

			message.setColor(ChipPlugin.COLOR_BASE);

			message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(String.join(", ", getModificationsWords(entity))).create()));

			notify(message);

			if (chip.removeEntity) {
				entity.remove();
			} else {
				chip.getEntityFixer().fix(entity);
			}
		}
	}

	public static void notify(BaseComponent message) {
		if (chip.chatNotifications) {
			Bukkit.getOnlinePlayers().forEach(p -> {
				if (p.hasPermission(ChipPlugin.PERMISSION_NOTIFY)) p.spigot().sendMessage(message);
			});
		}

		if (chip.consoleNotifications) {
			chip.getLogger().log(Level.INFO, message.toPlainText());
		}
	}

	public static void notifyItemStackCreated(Optional<String> description, ItemStack itemStack) {
		TextComponent message = new TextComponent(description.orElse("?") + " spawned " + itemStack.getType().name() + " which had modifications (hover for info)");

		message.setColor(ChipPlugin.COLOR_BASE);

		message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(String.join(", ", getModificationsWords(itemStack))).create()));

		notify(message);
	}

	public static void notifyItemStackUsed(Optional<String> description, ItemStack itemStack) {
		TextComponent message = new TextComponent(description.orElse("?") + " used " + itemStack.getType().name() + " which had modifications (hover for info)");

		message.setColor(ChipPlugin.COLOR_BASE);

		message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(String.join(", ", getModificationsWords(itemStack))).create()));

		notify(message);
	}
	
}
