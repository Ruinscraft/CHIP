package com.ruinscraft.chip.packetadapters;

import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.ruinscraft.chip.CHIPUtil;
import com.ruinscraft.chip.InvalidAttributeException;

public class SpawnEntityPacketAdapter extends PacketAdapter {

	public SpawnEntityPacketAdapter(JavaPlugin plugin) {
		super(plugin, PacketType.Play.Server.SPAWN_ENTITY);
	}

	@Override
	public void onPacketSending(PacketEvent event) {
		final PacketContainer packet = event.getPacket();

		if (packet.getType() != PacketType.Play.Server.SPAWN_ENTITY) {
			return;
		}
		
		for (Entity entity : packet.getEntityModifier(event).getValues()) {
			try {
				CHIPUtil.checkEntity(entity);
			} catch (InvalidAttributeException e) {
				event.setCancelled(true);
			}
		}
	}

}
