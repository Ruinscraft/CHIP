package com.ruinscraft.chip.checkers;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.inventory.ItemStack;

import com.google.common.cache.CacheLoader;
import com.ruinscraft.chip.Modification;

public class CheckerCacheLoader extends CacheLoader<Object, Set<Modification>> {

	private final Checker<ItemStack> itemStackChecker;
	
	public CheckerCacheLoader() {
		itemStackChecker = new ItemStackChecker();
	}
	
	@Override
	public Set<Modification> load(Object key) throws Exception {
		System.out.println("running check algorithm");
		
		if (key instanceof ItemStack) {
			return itemStackChecker.getModifications((ItemStack) key);
		}
		
		return new HashSet<>();
	}
	
}
