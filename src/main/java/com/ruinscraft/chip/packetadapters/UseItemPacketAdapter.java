package com.ruinscraft.chip.packetadapters;

import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.ruinscraft.chip.CHIPPlugin;
import com.ruinscraft.chip.CHIPUtil;

public class UseItemPacketAdapter extends PacketAdapter {

	public UseItemPacketAdapter(CHIPPlugin plugin) {
		super(plugin, PacketType.Play.Client.USE_ITEM);
	}
	
	@Override
	public void onPacketReceiving(PacketEvent event) {
		final Player player = event.getPlayer();
		final PacketContainer packet = event.getPacket();

		if (packet.getType() != PacketType.Play.Client.USE_ITEM) {
			return;
		}

		CHIPUtil.cleanInventory(player.getInventory());
	}
	
}
