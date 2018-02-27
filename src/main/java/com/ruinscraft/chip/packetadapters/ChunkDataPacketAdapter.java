package com.ruinscraft.chip.packetadapters;

import java.util.Optional;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.ruinscraft.chip.CHIPPlugin;

public class ChunkDataPacketAdapter extends PacketAdapter {

	private final JavaPlugin plugin;
	
	public ChunkDataPacketAdapter(JavaPlugin plugin) {
		super(plugin, PacketType.Play.Server.MAP_CHUNK);
		this.plugin = plugin;
	}

	@Override
	public void onPacketSending(PacketEvent event) {
		final Player player = event.getPlayer();
		final PacketContainer packet = event.getPacket();

		if (packet.getType() != PacketType.Play.Server.MAP_CHUNK) {
			return;
		}

		final World world = player.getWorld();

		final int chunkX = packet.getIntegers().read(0);
		final int chunkZ = packet.getIntegers().read(1);

		final Chunk chunk = world.getChunkAt(chunkX, chunkZ);

		int x = chunk.getX() << 4;
		int z = chunk.getZ() << 4;

		for (int xx = x; xx < x + 16; xx++) {
			for (int zz = z; zz < z + 16; zz++) {
				for (int yy = 0; yy < 256; yy++) {
					final Block block = world.getBlockAt(xx, yy, zz);
					
					if (block.getState() instanceof InventoryHolder) {
						final InventoryHolder inventoryHolder = (InventoryHolder) block.getState();
						
						plugin.getServer().getScheduler().runTask(plugin, () -> {
							CHIPPlugin.getInstance().getUtil().cleanInventory(Optional.of(block.getType().name() + " (" + block.getX() + ", " + block.getY() + ", " + block.getZ() + ")"), inventoryHolder.getInventory());
						});
					}
				}
			}
		}
	}
	
}
