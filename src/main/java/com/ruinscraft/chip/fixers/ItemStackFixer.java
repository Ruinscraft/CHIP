package com.ruinscraft.chip.fixers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.PotionMeta;

import com.ruinscraft.chip.ChipPlugin;
import com.ruinscraft.chip.Modification;

public class ItemStackFixer implements Fixer<ItemStack> {

	@Override
	public void fix(ItemStack itemStack) {
		for (Modification modification : ChipPlugin.getModifications(itemStack)) {
			switch (modification) {
			case ITEMSTACK_ENCHANTMENT_NOT_COMPATIBLE: {
				if (itemStack.getEnchantments() == null) {
					break;
				}

				for (Enchantment enchantment : itemStack.getEnchantments().keySet()) {
					itemStack.removeEnchantment(enchantment);
				}
			}

			case ITEMSTACK_ENCHANTMENT_TOO_HIGH: {
				if (itemStack.getEnchantments() == null) {
					break;
				}

				for (Enchantment enchantment : itemStack.getEnchantments().keySet()) {
					itemStack.removeEnchantment(enchantment);
				}
			}

			case ITEMSTACK_ENCHANTMENT_TOO_LOW: {
				if (itemStack.getEnchantments() == null) {
					break;
				}

				for (Enchantment enchantment : itemStack.getEnchantments().keySet()) {
					itemStack.removeEnchantment(enchantment);
				}
			}

			case ITEMSTACK_FIREWORK_NOT_CRAFTABLE: {
				if (itemStack.getEnchantments() == null) {
					break;
				}

				if (itemStack.getItemMeta() instanceof FireworkMeta) {
					final FireworkMeta fireworkMeta = (FireworkMeta) itemStack.getItemMeta();

					fireworkMeta.clearEffects();
				}
			}

			case ITEMSTACK_META_COLORED_LORE: {
				if (!itemStack.hasItemMeta()) {
					break;
				}

				if (!itemStack.getItemMeta().hasLore()) {
					break;
				}

				List<String> newLore = new ArrayList<>();

				itemStack.getItemMeta().getLore().forEach(l -> newLore.add(ChatColor.stripColor(l)));

				itemStack.getItemMeta().setLore(newLore);
			}

			case ITEMSTACK_META_COLORED_NAME: {
				if (!itemStack.hasItemMeta()) {
					break;
				}

				if (!itemStack.getItemMeta().hasDisplayName()) {
					break;
				}

				itemStack.getItemMeta().setDisplayName(ChatColor.stripColor(itemStack.getItemMeta().getDisplayName()));
			}

			case ITEMSTACK_META_CUSTOM_LORE: {
				if (!itemStack.hasItemMeta()) {
					break;
				}

				itemStack.getItemMeta().setLore(null);
			}

			case ITEMSTACK_META_LORE_TOO_LONG: {
				if (!itemStack.hasItemMeta()) {
					break;
				}

				if (!itemStack.getItemMeta().hasLore()) {
					break;
				}

				List<String> newLore = new ArrayList<>();

				for (String line : itemStack.getItemMeta().getLore()) {
					if (line.length() > ChipPlugin.getInstance().maxCustomLoreLength) {
						newLore.add(line.substring(0, ChipPlugin.getInstance().maxCustomLoreLength - 1));
					} else {
						newLore.add(line);
					}
				}

				itemStack.getItemMeta().setLore(newLore);
			}

			case ITEMSTACK_META_NAME_TOO_LONG: {
				if (!itemStack.hasItemMeta()) {
					break;
				}

				if (!itemStack.getItemMeta().hasDisplayName()) {
					break;
				}

				String name = itemStack.getItemMeta().getDisplayName();

				if (name.length() > ChipPlugin.getInstance().maxCustomNameLength) {
					itemStack.getItemMeta().setDisplayName(name.substring(0, ChipPlugin.getInstance().maxCustomNameLength - 1));
				}
			}

			case ITEMSTACK_META_UNBREAKABLE: {
				if (!itemStack.hasItemMeta()) {
					break;
				}

				itemStack.getItemMeta().setUnbreakable(false);
			}

			case ITEMSTACK_POTION_CUSTOM: {
				if (!(itemStack.getItemMeta() instanceof PotionMeta)) {
					break;
				}
				
				PotionMeta potionMeta = (PotionMeta) itemStack.getItemMeta();
				
				potionMeta.clearCustomEffects();
			}
			
			default: {
				break;
			}

			}

		}

	}

}
