package com.ruinscraft.chip;

import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;

import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;

public final class CHIPUtil {

	public static final int CRAFTING_SLOTS = 9;
	
	public static void checkEntity(Entity entity) throws InvalidAttributeException {
		if (entity.getCustomName() != null) {
			if (entity.getCustomName().length() > 32) {
				throw new InvalidAttributeException("Entity custom name was greater than 32 chars");
			}
		}
		
		if (entity.isInvulnerable()) {
			throw new InvalidAttributeException("Entity was invulnerable");
		}
		
		if (entity.isGlowing()) {
			throw new InvalidAttributeException("Entity was glowing");
		}
		
		if (entity.isCustomNameVisible()) {
			entity.setCustomNameVisible(false);
		}
		
		if (entity instanceof ArmorStand) {
			final ArmorStand armorStand = (ArmorStand) entity;

			if (armorStand.isSmall()) {
				armorStand.setSmall(false);
			}
			
			if (!armorStand.isVisible()) {
				armorStand.setVisible(true);
			}
			
			if (!armorStand.hasBasePlate()) {
				armorStand.setBasePlate(true);
			}
		}
	}
	
	public static void checkEnchantments(ItemStack itemStack) throws InvalidAttributeException {
		if (itemStack.getEnchantments() == null) {
			return;
		}
		
		for (int level : itemStack.getEnchantments().values()) {
			// probably should be 5 (ie sharpness V)
			if (level > 7) {
				throw new InvalidAttributeException("Enchantment level was greater than 7");
			}
		}
	}

	public static void checkNbt(ItemStack itemStack) throws InvalidAttributeException {
		NbtCompound nbtCompound = null;

		try {
			nbtCompound = (NbtCompound) NbtFactory.fromItemTag(itemStack);
		} catch (IllegalArgumentException e) {
			// not an instance of CraftItemStack
			return;
		}

		for (String key : nbtCompound.getKeys()) {
			if (key.contains("generic")) {
				throw new InvalidAttributeException("Contains modified attributes");
			}

			if (key.contains("CustomPotionEffects")) {
				throw new InvalidAttributeException("Contains modified potion effects");
			}
			
			if (key.contains("Size")) {
				throw new InvalidAttributeException("Contains modified spawn egg size");
			}
			
			if (key.contains("ExplosionRadius")) {
				throw new InvalidAttributeException("Contains modified spawn egg explosion radius");
			}
			
			if (key.contains("DeathLootTable")) {
				throw new InvalidAttributeException("Contains modified spawn egg drops on death");
			}
		}
	}

	public static void checkMeta(ItemStack itemStack) throws InvalidAttributeException {
		if (!itemStack.hasItemMeta()) {
			return;
		}

		final ItemMeta itemMeta = itemStack.getItemMeta();

		if (itemMeta.isUnbreakable()) {
			throw new InvalidAttributeException("Item was unbreakable");
		}

		if (itemMeta.hasDisplayName()) {
			if (itemMeta.getDisplayName().length() > 64) {
				throw new InvalidAttributeException("Display name was greater than 64 chars");
			}
		}

		if (itemMeta.hasLore()) {
			for (String lore : itemMeta.getLore()) {
				if (lore.length() > 128) {
					throw new InvalidAttributeException("Lore was greater than 128 chars");
				}
			}
		}

		if (itemMeta instanceof FireworkMeta) {
			final FireworkMeta fireworkMeta = (FireworkMeta) itemMeta;

			if (fireworkMeta.getPower() > 3) {
				throw new InvalidAttributeException("Firework power was greater than 3");
			}

			for (FireworkEffect fireworkEffect : fireworkMeta.getEffects()) {
				int specialEffects = 0;
				
				// firecharge + colors (max of 8)
				if (fireworkEffect.getFadeColors().size() + 1 > CRAFTING_SLOTS) {
					throw new InvalidAttributeException("Firework too many fade colors");
				}
				
				if (fireworkEffect.hasFlicker()) {
					specialEffects++;
				}
				
				if (fireworkEffect.hasTrail()) {
					specialEffects++;
				}
				
				if (fireworkEffect.getColors().size() + specialEffects > CRAFTING_SLOTS) {
					throw new InvalidAttributeException("Firework had too many options (colors/effects)");
				}
			}
		}
	}

	public static void checkItem(ItemStack itemStack) throws InvalidAttributeException {
		checkEnchantments(itemStack);
		checkNbt(itemStack);
		checkMeta(itemStack);
	}

	public static void cleanInventory(Inventory inventory) {
		for (ItemStack itemStack : inventory.getContents()) {
			if (itemStack == null) {
				continue;
			}
			
			if (itemStack.getType() == Material.AIR) {
				continue;
			}
			
			try {
				CHIPUtil.checkItem(itemStack);
			} catch (InvalidAttributeException e) {
				inventory.remove(itemStack);
			}
		}
	}

}
