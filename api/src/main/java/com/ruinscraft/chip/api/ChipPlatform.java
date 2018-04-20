package com.ruinscraft.chip.api;

import java.util.logging.Level;

public interface ChipPlatform {

	void log(Level level, String message);
	
	void runAsync(Runnable runnable);
	
	void runSync(Runnable runnable);

	<T> Checker<T> getChecker(Class<T> clazz);
	
}
