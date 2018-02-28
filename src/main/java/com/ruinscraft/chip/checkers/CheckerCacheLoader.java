package com.ruinscraft.chip.checkers;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.inventory.ItemStack;

import com.google.common.cache.CacheLoader;
import com.ruinscraft.chip.ChipPlugin;
import com.ruinscraft.chip.Modification;

public class CheckerCacheLoader extends CacheLoader<Object, Set<Modification>> {
	
	// entities can't be cached
	@Override
	public Set<Modification> load(Object key) throws Exception {
		if (key instanceof ItemStack) {
			return ChipPlugin.getInstance().getItemStackChecker().getModifications((ItemStack) key);
		}
		
		return new HashSet<>();
	}
	
}
