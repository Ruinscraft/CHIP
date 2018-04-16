package com.ruinscraft.chip.fixers;

import java.util.Set;

import com.ruinscraft.chip.Modification;

public interface Fixer<T> {

	void fix(T t, Set<Modification> modifications);
	
}
