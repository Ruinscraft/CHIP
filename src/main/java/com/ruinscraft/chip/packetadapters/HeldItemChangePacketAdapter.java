package com.ruinscraft.chip.packetadapters;

import java.util.Optional;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.ruinscraft.chip.ChipUtil;

public class HeldItemChangePacketAdapter extends PacketAdapter {

	public HeldItemChangePacketAdapter(JavaPlugin plugin) {
		super(plugin, PacketType.Play.Client.HELD_ITEM_SLOT);
	}

	@Override
	public void onPacketReceiving(PacketEvent event) {
		final Player player = event.getPlayer();

		ChipUtil.fix(player.getInventory().getItemInMainHand(), Optional.of(player.getName()));
	}

}
