package com.ruinscraft.chip.checkers;

import java.util.Set;

import com.ruinscraft.chip.Modification;

public interface Checker<T> {

	Set<Modification> getModifications(T t);
	
}
