package com.ruinscraft.chip.packetadapters;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.ruinscraft.chip.CHIPUtil;

public class HeldItemChangePacketAdapter extends PacketAdapter {

	private final JavaPlugin plugin;
	
	public HeldItemChangePacketAdapter(JavaPlugin plugin) {
		super(plugin, PacketType.Play.Client.HELD_ITEM_SLOT);
		this.plugin = plugin;
	}

	@Override
	public void onPacketReceiving(PacketEvent event) {
		final Player player = event.getPlayer();
		final PacketContainer packet = event.getPacket();

		if (packet.getType() != PacketType.Play.Client.HELD_ITEM_SLOT) {
			return;
		}

		plugin.getServer().getScheduler().runTask(plugin, () -> {
			CHIPUtil.cleanInventory(player.getInventory());
		});
	}

}
