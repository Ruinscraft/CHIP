package com.ruinscraft.chip.fixers;

import java.util.Set;

import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;

import com.ruinscraft.chip.ChipPlugin;
import com.ruinscraft.chip.Modification;

public class EntityFixer implements Fixer<Entity> {

	@Override
	public Entity fix(Entity entity, Set<Modification> modifications) {
		for (Modification modification : modifications) {
			switch (modification) {
			case ENTITY_ARMOR_STAND_BASE_PLATE: {
				if (!(entity instanceof ArmorStand)) {
					break;
				}

				ArmorStand armorStand = (ArmorStand) entity;

				armorStand.setBasePlate(true);

				break;
			}

			case ENTITY_ARMOR_STAND_SMALL: {
				if (!(entity instanceof ArmorStand)) {
					break;
				}

				ArmorStand armorStand = (ArmorStand) entity;

				armorStand.setSmall(false);

				break;
			}

			case ENTITY_ARMOR_STAND_VISIBLE: {
				if (!(entity instanceof ArmorStand)) {
					break;
				}

				ArmorStand armorStand = (ArmorStand) entity;

				armorStand.setVisible(true);

				break;
			}
			
			case ENTITY_ARMOR_STAND_NO_ARMS: {
				if (!(entity instanceof ArmorStand)) {
					break;
				}

				ArmorStand armorStand = (ArmorStand) entity;

				armorStand.setArms(false);
				
				break;
			}

			case ENTITY_CUSTOM_NAME_TOO_LONG: {
				if (entity.getCustomName().length() > ChipPlugin.getInstance().maxCustomNameLength) {
					entity.setCustomName(entity.getCustomName().substring(0, ChipPlugin.getInstance().maxCustomNameLength - 1));
				}

				break;
			}

			case ENTITY_CUSTOM_NAME_VISIBLE: {
				entity.setCustomNameVisible(false);

				break;
			}

			case ENTITY_GLOWING: {
				entity.setGlowing(false);

				break;
			}

			case ENTITY_INVULNERABLE: {
				entity.setInvulnerable(false);

				break;
			}

			default: {
				break;
			}

			}
		}
		
		return entity;
	}

}
