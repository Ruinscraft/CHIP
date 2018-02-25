package com.ruinscraft.chip.listeners;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.bukkit.Chunk;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;

public class WorldListener implements Listener {

	public static final int MAX_REDSTONE_UPDATES_PER_CHUNK_PER_MINUTE = 2000;

	private Map<Chunk, AreaRedstoneUpdates> chunkRedstoneUpdates = new ConcurrentHashMap<>();

	private final class AreaRedstoneUpdates {

		private long updates;
		private long lastUpdated;

		public AreaRedstoneUpdates(long updates, long lastUpdated) {
			this.updates = updates;
			this.lastUpdated = lastUpdated;
		}

		public long getUpdates() {
			return updates;
		}

		public void incrementUpdates() {
			updates++;
		}

		public void resetUpdates() {
			updates = 0;
		}

		public long getLastUpdated() {
			return lastUpdated;
		}

		public void setLastUpdated(long lastUpdated) {
			this.lastUpdated = lastUpdated;
		}

	}

	@EventHandler
	public void onBlockRedstone(BlockRedstoneEvent event) {

		final Chunk chunk = event.getBlock().getLocation().getChunk();
		
		AreaRedstoneUpdates areaRedstoneUpdates = null;
		
		if (chunkRedstoneUpdates.containsKey(chunk)) {
			areaRedstoneUpdates = chunkRedstoneUpdates.get(chunk);

			if (areaRedstoneUpdates.getUpdates() >= MAX_REDSTONE_UPDATES_PER_CHUNK_PER_MINUTE) {
				if (areaRedstoneUpdates.getLastUpdated() > System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(5)) {
					areaRedstoneUpdates.resetUpdates();
				} else {
					event.setNewCurrent(0);
				}
			} else {
				areaRedstoneUpdates.incrementUpdates();
				areaRedstoneUpdates.setLastUpdated(System.currentTimeMillis());
			}
		} else {
			areaRedstoneUpdates = new AreaRedstoneUpdates(1, System.currentTimeMillis());
		}
		
		chunkRedstoneUpdates.put(chunk, areaRedstoneUpdates);
		System.out.println(areaRedstoneUpdates.getUpdates());
	}

}
