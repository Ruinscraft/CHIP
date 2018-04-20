package com.ruinscraft.chip.bukkit;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.reflect.TypeToken;
import com.ruinscraft.chip.api.Checker;
import com.ruinscraft.chip.api.ChipPlatform;

public class ChipPluginBukkit extends JavaPlugin implements ChipPlatform {

	private Set<Checker<?>> checkers;
	
	@Override
	public void onEnable() {
		
		checkers = new HashSet<>();
		
	}

	@Override
	public void onDisable() {

		checkers.forEach(c -> c.clearCache());
		checkers.clear();
		
	}

	@Override
	public void log(Level level, String message) {
		getLogger().log(level, message);
	}

	@Override
	public void runAsync(Runnable runnable) {
		getServer().getScheduler().runTaskAsynchronously(this, runnable);
	}

	@Override
	public void runSync(Runnable runnable) {
		getServer().getScheduler().runTask(this, runnable);
	}

	@Override
	public <T> Checker<T> getChecker(Class<T> clazz) {

		TypeToken<T> typeToken = TypeToken.of(clazz);
		
		for (Checker<?> checker : checkers) {
			if (checker.getType().equals(typeToken)) {
				return (Checker<T>) checker;
			}
		}

		
		
		return null;
		
	}

}
