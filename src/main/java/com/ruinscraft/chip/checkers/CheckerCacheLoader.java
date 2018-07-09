package com.ruinscraft.chip.checkers;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import com.google.common.cache.CacheLoader;
import com.ruinscraft.chip.ChipPlugin;
import com.ruinscraft.chip.Modification;

public class CheckerCacheLoader extends CacheLoader<Object, Set<Modification>> {
	
	@Override
	public Set<Modification> load(Object key) throws Exception {
		if (key instanceof ItemStack) {
			return ChipPlugin.getInstance().getItemStackChecker().getModifications((ItemStack) key);
		}
		
		// possibly use entity UUID as cache in the future?
		else if (key instanceof Entity) {
			return ChipPlugin.getInstance().getEntityChecker().getModifications((Entity) key);
		}
		
		return new HashSet<>();
	}
	
}
