package com.ruinscraft.chip.packetadapters;

import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.ruinscraft.chip.CHIPPlugin;
import com.ruinscraft.chip.CHIPUtil;

public class HeldItemChangePacketAdapter extends PacketAdapter {

	public HeldItemChangePacketAdapter(CHIPPlugin plugin) {
		super(plugin, PacketType.Play.Client.HELD_ITEM_SLOT);
	}

	@Override
	public void onPacketReceiving(PacketEvent event) {
		final Player player = event.getPlayer();
		final PacketContainer packet = event.getPacket();

		if (packet.getType() != PacketType.Play.Client.HELD_ITEM_SLOT) {
			return;
		}

		CHIPUtil.cleanInventory(player.getInventory());
	}

}
