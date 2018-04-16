package com.ruinscraft.chip.packetadapters;

import java.util.Optional;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.ruinscraft.chip.ChipUtil;

public class UseItemPacketAdapter extends PacketAdapter {

	public UseItemPacketAdapter(JavaPlugin plugin) {
		super(plugin, PacketType.Play.Client.USE_ITEM);
	}

	@Override
	public void onPacketReceiving(PacketEvent event) {
		final Player player = event.getPlayer();
		final PacketContainer packet = event.getPacket();

		for (ItemStack itemStack : packet.getItemModifier().getValues()) {
			ChipUtil.fix(itemStack, Optional.of(player.getName()));
			// TODO: cancel?
		}
	}

}
