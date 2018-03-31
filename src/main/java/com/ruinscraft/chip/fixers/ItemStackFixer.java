package com.ruinscraft.chip.fixers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;

import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
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

				itemStack.getEnchantments().keySet().forEach(e -> itemStack.removeEnchantment(e));
			}

			case ITEMSTACK_ENCHANTMENT_TOO_HIGH: {
				if (itemStack.getEnchantments() == null) {
					break;
				}

				itemStack.getEnchantments().keySet().forEach(e -> itemStack.removeEnchantment(e));
			}

			case ITEMSTACK_ENCHANTMENT_TOO_LOW: {
				if (itemStack.getEnchantments() == null) {
					break;
				}

				itemStack.getEnchantments().keySet().forEach(e -> itemStack.removeEnchantment(e));
			}

			case ITEMSTACK_FIREWORK_NOT_CRAFTABLE: {
				if (itemStack.getEnchantments() == null) {
					break;
				}

				if (itemStack.getItemMeta() instanceof FireworkMeta) {
					final FireworkMeta fireworkMeta = (FireworkMeta) itemStack.getItemMeta();

					fireworkMeta.clearEffects();
					
					fireworkMeta.setPower(1);
					
					itemStack.setItemMeta(fireworkMeta);
				}
			}

			case ITEMSTACK_META_COLORED_LORE: {
				if (!itemStack.hasItemMeta()) {
					break;
				}

				ItemMeta itemMeta = itemStack.getItemMeta();
				
				if (!itemMeta.hasLore()) {
					break;
				}

				List<String> newLore = new ArrayList<>();

				itemMeta.getLore().forEach(l -> newLore.add(ChatColor.stripColor(l)));

				itemMeta.setLore(newLore);
				
				itemStack.setItemMeta(itemMeta);
			}

			case ITEMSTACK_META_COLORED_NAME: {
				if (!itemStack.hasItemMeta()) {
					break;
				}
				
				ItemMeta itemMeta = itemStack.getItemMeta();

				if (!itemMeta.hasDisplayName()) {
					break;
				}

				String stripped = ChatColor.stripColor(itemMeta.getDisplayName());
				
				itemMeta.setDisplayName(stripped);
				
				itemStack.setItemMeta(itemMeta);
			}

			case ITEMSTACK_META_CUSTOM_LORE: {
				if (!itemStack.hasItemMeta()) {
					break;
				}

				ItemMeta itemMeta = itemStack.getItemMeta();
				
				itemMeta.setLore(null);
				
				itemStack.setItemMeta(itemMeta);
			}

			case ITEMSTACK_META_LORE_TOO_LONG: {
				if (!itemStack.hasItemMeta()) {
					break;
				}
				
				ItemMeta itemMeta = itemStack.getItemMeta();

				if (!itemMeta.hasLore()) {
					break;
				}

				List<String> newLore = new ArrayList<>();

				for (String line : itemMeta.getLore()) {
					if (line.trim().length() > ChipPlugin.getInstance().maxCustomLoreLength) {
						newLore.add(line.substring(0, ChipPlugin.getInstance().maxCustomLoreLength - 1));
					} else {
						newLore.add(line);
					}
				}

				itemMeta.setLore(newLore);
				
				itemStack.setItemMeta(itemMeta);
			}

			case ITEMSTACK_META_NAME_TOO_LONG: {
				if (!itemStack.hasItemMeta()) {
					break;
				}

				ItemMeta itemMeta = itemStack.getItemMeta();
				
				if (!itemMeta.hasDisplayName()) {
					break;
				}

				String name = itemMeta.getDisplayName();

				if (name.trim().length() > ChipPlugin.getInstance().maxCustomNameLength) {
					itemMeta.setDisplayName(name.substring(0, ChipPlugin.getInstance().maxCustomNameLength - 1));
				}
				
				itemStack.setItemMeta(itemMeta);
			}

			case ITEMSTACK_NBT_UNBREAKABLE: {
				removeNbt(itemStack, "Unbreakable");
			}

			case ITEMSTACK_NBT_POTION_CUSTOM: {
				removeNbt(itemStack, "CustomPotionEffects");
			}
			
			case ITEMSTACK_NBT_MODIFIERS: {
				removeNbt(itemStack, "AttributeModifiers");
			}
			
			case ITEMSTACK_NBT_SIZE: {
				removeNbt(itemStack, "Size");
			}
			
			case ITEMSTACK_NBT_DEATH_LOOT: {
				removeNbt(itemStack, "DeathLootTable");
			}
			
			case ITEMSTACK_NBT_ENTITY_TAG: {
				removeNbt(itemStack, "EntityTag");
			}
			
			case ITEMSTACK_NBT_EXPLOSION_RADIUS: {
				removeNbt(itemStack, "ExplosionRadius");
			}
			
			case ITEMSTACK_NBT_TILE_ENTITY_DATA: {
				removeNbt(itemStack, "TileEntityData");
			}
			
			default: {
				break;
			}

			}
		}
	}
	
	private static void removeNbt(ItemStack itemStack, String nbtTag) {
		NbtCompound nbtCompound = null;

		try {
			nbtCompound = (NbtCompound) NbtFactory.fromItemTag(itemStack);
		} catch (IllegalArgumentException e) {
			// not an instance of CraftItemStack
			return;
		}
		
		if (nbtCompound.containsKey(nbtTag)) {
			nbtCompound.remove(nbtTag);
		}
		
		NbtFactory.setItemTag(itemStack, nbtCompound);
	}

}
