package com.ruinscraft.chip.packetadapters;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.ruinscraft.chip.CHIPPlugin;
import com.ruinscraft.chip.CHIPUtil;
import com.ruinscraft.chip.InvalidAttributeException;

public class SetCreativeSlotPacketAdapter extends PacketAdapter {

	public SetCreativeSlotPacketAdapter(CHIPPlugin plugin) {
		super(plugin, PacketType.Play.Client.SET_CREATIVE_SLOT);
	}

	@Override
	public void onPacketReceiving(PacketEvent event) {
		final Player player = event.getPlayer();
		final PacketContainer packet = event.getPacket();

		if (packet.getType() != PacketType.Play.Client.SET_CREATIVE_SLOT) {
			return;
		}

		for (ItemStack itemStack : packet.getItemModifier().getValues()) {
			try {
				CHIPUtil.checkItem(itemStack);
			} catch (InvalidAttributeException e) {
				event.setCancelled(true);
				player.sendMessage("No hacked item creation");
			}
		}
	}

}
