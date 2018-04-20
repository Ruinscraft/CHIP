package com.ruinscraft.chip.api;

import java.util.Set;

import com.google.common.cache.Cache;
import com.google.common.reflect.TypeToken;

public interface Checker<T> {

	Cache<T, Set<Modification>> getCache();
	
	TypeToken<T> getType();
	
	void clearCache();
	
	Set<Modification> check(T t);
	
	Set<Modification> forceCheck(T t);
	
}
