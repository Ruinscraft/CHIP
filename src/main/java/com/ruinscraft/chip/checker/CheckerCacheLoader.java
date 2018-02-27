package com.ruinscraft.chip.checker;

import java.util.Set;

import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import com.google.common.cache.CacheLoader;
import com.google.common.collect.Sets;
import com.ruinscraft.chip.Modification;

public class CheckerCacheLoader extends CacheLoader<Object, Set<Modification>> {

	private final Checker<Entity> entityChecker;
	private final Checker<ItemStack> itemStackChecker;
	
	public CheckerCacheLoader() {
		entityChecker = new EntityChecker();
		itemStackChecker = new ItemStackChecker();
	}
	
	@Override
	public Set<Modification> load(Object key) throws Exception {
		if (key instanceof Entity) {
			return entityChecker.getModifications((Entity) key);
		}
		
		if (key instanceof ItemStack) {
			return itemStackChecker.getModifications((ItemStack) key);
		}
		
		return Sets.newHashSet();
	}
	
}
