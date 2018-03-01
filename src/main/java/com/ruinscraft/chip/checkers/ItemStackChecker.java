package com.ruinscraft.chip.checkers;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.FireworkEffect;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;

import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import com.ruinscraft.chip.ChipPlugin;
import com.ruinscraft.chip.Modification;

public class ItemStackChecker implements Checker<ItemStack> {

	private static final ChipPlugin chip = ChipPlugin.getInstance();
	private static final int CRAFTING_SLOTS = 9;
	private static final int FIREWORK_MAX_POWER = 3;

	@Override
	public Set<Modification> getModifications(ItemStack itemStack) {
		Set<Modification> modifications = new HashSet<>();

		for (Map.Entry<Enchantment, Integer> enchantmentEntry : itemStack.getEnchantments().entrySet()) {
			final Enchantment enchantment = enchantmentEntry.getKey();
			final int level = enchantmentEntry.getValue();

			if (!chip.aboveNormalEnchants) {
				if (level > enchantment.getMaxLevel()) {
					modifications.add(Modification.ITEMSTACK_ENCHANTMENT_TOO_HIGH);
				}
			}

			if (!chip.belowNormalEnchants) {
				if (level < enchantment.getStartLevel()) {
					modifications.add(Modification.ITEMSTACK_ENCHANTMENT_TOO_LOW);
				}
			}

			for (Enchantment itemEnchantment : itemStack.getEnchantments().keySet()) {
				if (itemEnchantment.equals(enchantment)) {
					continue;
				}

				if (!chip.conflictingEnchants) {
					if (enchantment.conflictsWith(itemEnchantment)) {
						modifications.add(Modification.ITEMSTACK_ENCHANTMENT_NOT_COMPATIBLE);
					}
				}
			}
		}

		NbtCompound nbtCompound = null;

		try {
			nbtCompound = (NbtCompound) NbtFactory.fromItemTag(itemStack);
		} catch (IllegalArgumentException e) {
			// not an instance of CraftItemStack
			return modifications;
		}

		for (String key : nbtCompound.getKeys()) {

			if (key.contains("generic")) {
			}

			if (!chip.customPotions) {
				if (key.contains("CustomPotionEffects")) {
					modifications.add(Modification.ITEMSTACK_POTION_CUSTOM);
				}
			}

			// TODO look into these
			if (key.contains("Size")) {
			}

			if (key.contains("ExplosionRadius")) {
			}

			if (key.contains("DeathLootTable")) {
			}

			if (key.contains("TileEntityData")) {
			}
		}

		if (!itemStack.hasItemMeta()) {
			return modifications;
		}

		final ItemMeta itemMeta = itemStack.getItemMeta();

		if (!chip.coloredCustomNames) {
			if (itemMeta.hasDisplayName()) {
				String stripped = ChatColor.stripColor(itemMeta.getDisplayName());
				if (!stripped.equals(itemMeta.getDisplayName())) {
					modifications.add(Modification.ITEMSTACK_META_COLORED_NAME);
				}
			}
		}

		if (!chip.coloredCustomLore) {
			if (itemMeta.hasLore()) {
				for (String line : itemMeta.getLore()) {
					String stripped = ChatColor.stripColor(line);
					if (!stripped.equals(line)) {
						modifications.add(Modification.ITEMSTACK_META_COLORED_LORE);
						break;
					}
				}
			}
		}

		if (!chip.unbreakableItems) {
			if (itemMeta.isUnbreakable()) {
				modifications.add(Modification.ITEMSTACK_META_UNBREAKABLE);
			}
		}

		if (itemMeta.hasDisplayName()) {
			if (itemMeta.getDisplayName().length() > chip.maxCustomNameLength) {
				modifications.add(Modification.ITEMSTACK_META_NAME_TOO_LONG);
			}
		}

		if (itemMeta.hasLore()) {
			if (!chip.customLore) {
				// TODO check if vanilla items have lore
				modifications.add(Modification.ITEMSTACK_META_CUSTOM_LORE);
			}
			for (String line : itemMeta.getLore()) {
				if (line.length() > chip.maxCustomLoreLength) {
					modifications.add(Modification.ITEMSTACK_META_LORE_TOO_LONG);
				}
			}
		}

		if (!chip.nonCraftableFireworks) {
			if (itemMeta instanceof FireworkMeta) {
				final FireworkMeta fireworkMeta = (FireworkMeta) itemMeta;

				if (fireworkMeta.getPower() > FIREWORK_MAX_POWER) {
					modifications.add(Modification.ITEMSTACK_FIREWORK_NOT_CRAFTABLE);
				}

				for (FireworkEffect fireworkEffect : fireworkMeta.getEffects()) {
					int specialEffects = 0;

					// firecharge + colors (max of 8)
					if (fireworkEffect.getFadeColors().size() + 1 > CRAFTING_SLOTS) {
						modifications.add(Modification.ITEMSTACK_FIREWORK_NOT_CRAFTABLE);
					}

					if (fireworkEffect.hasFlicker()) {
						specialEffects++;
					}

					if (fireworkEffect.hasTrail()) {
						specialEffects++;
					}

					if (fireworkEffect.getColors().size() + specialEffects > CRAFTING_SLOTS) {
						modifications.add(Modification.ITEMSTACK_FIREWORK_NOT_CRAFTABLE);
					}
				}
			}
		}

		return modifications;
	}

}
