package com.ruinscraft.chip.bukkit;

import java.util.Set;

import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.google.common.reflect.TypeToken;
import com.ruinscraft.chip.api.GenericChecker;
import com.ruinscraft.chip.api.Modification;

public class NbtCompoundChecker extends GenericChecker<NbtCompound> {

	public NbtCompoundChecker() {
		super(TypeToken.of(NbtCompound.class));
	}

	@Override
	public Set<Modification> forceCheck(NbtCompound nbtCompound) {

		return null;
		
	}
	
}
