package com.ruinscraft.chip.packetadapters;

import java.util.Optional;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.ruinscraft.chip.ChipPlugin;

public class UseItemPacketAdapter extends PacketAdapter {

	private final JavaPlugin plugin;
	
	public UseItemPacketAdapter(JavaPlugin plugin) {
		super(plugin, PacketType.Play.Client.USE_ITEM);
		this.plugin = plugin;
	}
	
	@Override
	public void onPacketReceiving(PacketEvent event) {
		final Player player = event.getPlayer();
		final PacketContainer packet = event.getPacket();

		if (packet.getType() != PacketType.Play.Client.USE_ITEM) {
			return;
		}

		plugin.getServer().getScheduler().runTask(plugin, () -> {
			ChipPlugin.cleanInventory(Optional.of(player.getName()), player.getInventory());
		});
	}
	
}
