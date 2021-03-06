package com.ruinscraft.chip.checkers;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.block.ShulkerBox;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;

import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import com.ruinscraft.chip.ChipPlugin;
import com.ruinscraft.chip.Modification;
import com.ruinscraft.chip.util.ChipUtil;

public class ItemStackChecker implements Checker<ItemStack> {

	private static final ChipPlugin chip = ChipPlugin.getInstance();
	private static final int CRAFTING_SLOTS = 9;
	private static final int FIREWORK_MAX_POWER = 3;
	private static final int FIREWORK_MIN_POWER = 1;

	@Override
	public Set<Modification> getModifications(ItemStack itemStack) {
		Set<Modification> modifications = new HashSet<>();

		if (itemStack == null) {
			return modifications;
		}

		if (itemStack.getType() == Material.AIR) {
			return modifications;
		}

		final ItemMeta itemMeta = itemStack.getItemMeta();

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

			if (!chip.conflictingEnchants) {
				for (Enchantment itemEnchantment : itemStack.getEnchantments().keySet()) {
					if (itemEnchantment.equals(enchantment)) {
						continue;
					}

					if (enchantment.conflictsWith(itemEnchantment)) {
						modifications.add(Modification.ITEMSTACK_ENCHANTMENT_NOT_COMPATIBLE);
					}
				}
			}
		}

		NbtCompound base = null;

		try {
			base = (NbtCompound) NbtFactory.fromItemTag(itemStack);
		} catch (IllegalArgumentException e) {
			// not an instance of CraftItemStack
			return modifications;
		}

		for (String key : base.getKeys()) {
			if (!chip.attributeModifiers) {
				if (key.equals("AttributeModifiers")) {
					modifications.add(Modification.ITEMSTACK_NBT_MODIFIERS);
				}
			}

			if (!chip.size) {
				if (key.equals("Size")) {
					modifications.add(Modification.ITEMSTACK_NBT_SIZE);
				}
			}

			if (!chip.explosionRadius) {
				if (key.equals("ExplosionRadius")) {
					modifications.add(Modification.ITEMSTACK_NBT_EXPLOSION_RADIUS);
				}
			}

			if (!chip.deathLootTable) {
				if (key.equals("DeathLootTable")) {
					modifications.add(Modification.ITEMSTACK_NBT_DEATH_LOOT);
				}
			}

			if (!chip.tileEntityData) {
				if (key.equals("TileEntityData")) {
					modifications.add(Modification.ITEMSTACK_NBT_TILE_ENTITY_DATA);
				}
			}

			if (!chip.blockEntityTag) {
				boolean prevent = true;

				if (itemMeta instanceof BlockStateMeta) {
					BlockStateMeta blockStateMeta = (BlockStateMeta) itemMeta;

					if (blockStateMeta.getBlockState() instanceof ShulkerBox) {
						prevent = false;
					}
				}

				if (key.equals("BlockEntityTag") && prevent) {
					if (base.getCompound(key).containsKey("Items")) {
						modifications.add(Modification.ITEMSTACK_NBT_BLOCK_ENTITY_TAG);
					}
				}
			}
		}

		if (!itemStack.hasItemMeta()) {
			return modifications;
		}

		if (!chip.customPotions) {
			if (itemMeta instanceof PotionMeta) {
				final PotionMeta potionMeta = (PotionMeta) itemMeta;

				if (potionMeta.hasCustomEffects()) {
					modifications.add(Modification.ITEMSTACK_NBT_POTION_CUSTOM);
				}
			}
		}

		if (!chip.coloredCustomNames) {
			if (chip.ignoreHeadNames && itemStack.getType() == Material.PLAYER_HEAD) {
				return modifications;
			}

			if (itemMeta.hasDisplayName()) {
				String stripped = ChatColor.stripColor(itemMeta.getDisplayName());
				if (!stripped.equals(itemMeta.getDisplayName())) {
					modifications.add(Modification.ITEMSTACK_META_COLORED_NAME);
				}
			}
		}

		if (!chip.coloredCustomLore) {
			if (chip.ignoreHeadLores && itemStack.getType() == Material.PLAYER_HEAD) {
				return modifications;
			}

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
				modifications.add(Modification.ITEMSTACK_NBT_UNBREAKABLE);
			}
		}

		if (itemMeta.hasDisplayName()) {
			if (itemMeta.getDisplayName().length() > chip.maxCustomNameLength) {
				modifications.add(Modification.ITEMSTACK_META_NAME_TOO_LONG);
			}
		}

		if (!chip.customLore) {
			if (itemMeta.hasLore()) {
				if (chip.ignoreHeadLores && itemStack.getType() == Material.PLAYER_HEAD) {
					return modifications;
				}

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

				if (fireworkMeta.getPower() < FIREWORK_MIN_POWER) {
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

		if (itemMeta instanceof BookMeta) {
			BookMeta bookMeta = (BookMeta) itemMeta;

			if (ChipUtil.bookKnownForged(bookMeta, itemStack)) {
				modifications.add(Modification.ITEMSTACK_BOOK_FORGED);
			}
		}

		return modifications;
	}

}
