package com.ruinscraft.chip.checkers;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;

import com.ruinscraft.chip.ChipPlugin;
import com.ruinscraft.chip.Modification;

public class EntityChecker implements Checker<Entity> {

	private static final ChipPlugin chip = ChipPlugin.getInstance();

	@Override
	public Set<Modification> getModifications(Entity entity) {
		Set<Modification> modifications = new HashSet<>();
		
		if (entity == null) {
			return modifications;
		}
		
		if (!ChipPlugin.is1_8()) {
			if (!chip.invulnerable) {
				if (entity.isInvulnerable()) {
					modifications.add(Modification.ENTITY_INVULNERABLE);
				}
			}
			
			if (!chip.glowing) {
				if (entity.isGlowing()) {
					modifications.add(Modification.ENTITY_GLOWING);
				}
			}
			
			if (!chip.customNameVisible) {
				if (entity.isCustomNameVisible()) {
					modifications.add(Modification.ENTITY_CUSTOM_NAME_VISIBLE);
				}
			}
		}

		if (entity instanceof ArmorStand) {
			final ArmorStand armorStand = (ArmorStand) entity;
			
			if (!chip.smallArmorStands) {
				if (armorStand.isSmall()) {
					modifications.add(Modification.ENTITY_ARMOR_STAND_SMALL);
				}
			}

			if (!chip.visibleArmorStands) {
				if (!armorStand.isVisible()) {
					modifications.add(Modification.ENTITY_ARMOR_STAND_VISIBLE);
				}
			}

			if (!chip.basePlateArmorStands) {
				if (!armorStand.hasBasePlate()) {
					modifications.add(Modification.ENTITY_ARMOR_STAND_BASE_PLATE);
				}
			}
			
			if (!chip.arms) {
				if (armorStand.hasArms()) {
					modifications.add(Modification.ENTITY_ARMOR_STAND_ARMS);
				}
			}
		}
		
		return modifications;
	}

}
