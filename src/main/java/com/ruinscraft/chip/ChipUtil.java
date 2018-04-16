package com.ruinscraft.chip;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class ChipUtil {

	private static final ChipPlugin chip = ChipPlugin.getInstance();

	public static Set<Modification> check(Object o) {
		return chip.getCheckerCache().getUnchecked(o);
	}

	public static List<String> getWords(Set<Modification> modifications) {
		return modifications.stream().map(Modification::getPretty).collect(Collectors.toList());
	}

	public static boolean hasModifications(Object o) {
		return !check(o).isEmpty();
	}

	public static void fix(Object o, Optional<String> parent) {
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
			
			Set<Modification> modifications = check(itemStack);
			
			if (!modifications.isEmpty()) {
				chip.getItemStackFixer().fix(itemStack, modifications);
				
				notify(Optional.of(itemStack.getType().name()), parent, Optional.empty(), modifications);
			}
		}
		
		else if (o instanceof Entity) {
			Entity entity = (Entity) o;
			
			Set<Modification> modifications = check(entity);
			
			if (!modifications.isEmpty()) {
				chip.getEntityFixer().fix(entity, modifications);

				notify(Optional.of(entity.getType().name()), 
						parent, 
						Optional.of(entity.getLocation().getX() + ", " + entity.getLocation().getY() + ", " + entity.getLocation().getZ()), 
						modifications);
			}
		}
	}
	
	public static void fixInventory(Inventory inventory, Optional<String> parent) {
		inventory.forEach(itemStack -> fix(itemStack, parent));
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
