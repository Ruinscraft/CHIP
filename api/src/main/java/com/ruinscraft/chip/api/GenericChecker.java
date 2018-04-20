package com.ruinscraft.chip.api;

import java.util.Set;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.reflect.TypeToken;

public abstract class GenericChecker<T> implements Checker<T> {

	private LoadingCache<T, Set<Modification>> cache;
	private TypeToken<T> typeToken;

	public GenericChecker(TypeToken<T> typeToken) {
		this.typeToken = typeToken;
	}
	
	@Override
	public Cache<T, Set<Modification>> getCache() {
		if (cache == null) {
			cache = CacheBuilder
					.newBuilder()
					.maximumSize(8000)
					.build(new GenericCheckerCacheLoader());
		}
		
		return cache;
	}

	@Override
	public TypeToken<T> getType() {
		return typeToken;
	}

	@Override
	public void clearCache() {
		cache.invalidateAll();
	}

	@Override
	public Set<Modification> check(T t) {
		return cache.getUnchecked(t);
	}
	
	private class GenericCheckerCacheLoader extends CacheLoader<T, Set<Modification>> {
		@Override
		public Set<Modification> load(T t) throws Exception {
			return forceCheck(t);
		}
	}

}
