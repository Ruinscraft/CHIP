package com.ruinscraft.chip.packetadapters;

import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.ruinscraft.chip.ChipUtil;

public class SpawnEntityPacketAdapter extends PacketAdapter {

	public SpawnEntityPacketAdapter(JavaPlugin plugin) {
		super(plugin, PacketType.Play.Server.SPAWN_ENTITY);
	}

	@Override
	public void onPacketSending(PacketEvent event) {
		final PacketContainer packet = event.getPacket();
		
		for (Entity entity : packet.getEntityModifier(event).getValues()) {
			if (ChipUtil.hasModifications(entity)) {
				// TODO: fix or cancel?
				event.setCancelled(true);
			}
		}
	}

}
