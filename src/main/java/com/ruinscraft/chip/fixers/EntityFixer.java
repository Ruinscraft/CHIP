package com.ruinscraft.chip.fixers;

import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;

import com.ruinscraft.chip.ChipPlugin;
import com.ruinscraft.chip.Modification;

public class EntityFixer implements Fixer<Entity> {

	@Override
	public void fix(Entity entity) {
		for (Modification modification : ChipPlugin.getModifications(entity)) {
			switch (modification) {
			case ENTITY_ARMOR_STAND_BASE_PLATE: {
				if (!(entity instanceof ArmorStand)) {
					break;
				}
				
				ArmorStand armorStand = (ArmorStand) entity;
				
				armorStand.setBasePlate(false);
			}
			
			case ENTITY_ARMOR_STAND_SMALL: {
				if (!(entity instanceof ArmorStand)) {
					break;
				}
				
				ArmorStand armorStand = (ArmorStand) entity;
				
				armorStand.setSmall(false);
			}
			
			case ENTITY_ARMOR_STAND_VISIBLE: {
				if (!(entity instanceof ArmorStand)) {
					break;
				}
				
				ArmorStand armorStand = (ArmorStand) entity;
				
				armorStand.setVisible(true);
			}
			
			case ENTITY_CUSTOM_NAME_TOO_LONG: {
				if (entity.getCustomName().length() > ChipPlugin.getInstance().maxCustomNameLength) {
					entity.setCustomName(entity.getCustomName().substring(0, ChipPlugin.getInstance().maxCustomNameLength - 1));
				}
			}
			
			case ENTITY_CUSTOM_NAME_VISIBLE: {
				entity.setCustomNameVisible(false);
			}
			
			case ENTITY_GLOWING: {
				entity.setGlowing(false);
			}

			case ENTITY_INVULNERABLE: {
				entity.setInvulnerable(false);
			}
			
			default: {
				break;
			}
			
			}
		}
	}
	
}
