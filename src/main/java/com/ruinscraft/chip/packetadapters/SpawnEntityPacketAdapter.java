package com.ruinscraft.chip.packetadapters;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.ruinscraft.chip.CHIPPlugin;
import com.ruinscraft.chip.CHIPUtil;
import com.ruinscraft.chip.InvalidAttributeException;

public class SpawnEntityPacketAdapter extends PacketAdapter {

	public SpawnEntityPacketAdapter(CHIPPlugin plugin) {
		super(plugin, PacketType.Play.Server.SPAWN_ENTITY);
	}

	@Override
	public void onPacketSending(PacketEvent event) {
		final Player player = event.getPlayer();
		final PacketContainer packet = event.getPacket();

		if (packet.getType() != PacketType.Play.Server.SPAWN_ENTITY) {
			return;
		}
		
		for (Entity entity : packet.getEntityModifier(event).getValues()) {
			if (entity == null) {
				continue;
			}
		
			try {
				player.sendMessage(entity.getType().name());
				CHIPUtil.checkEntity(entity);
			} catch (InvalidAttributeException e) {
				player.sendMessage(e.getReason());
			}
		}
	}

}
