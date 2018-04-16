package com.ruinscraft.chip.packetadapters;

import java.util.Optional;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.ruinscraft.chip.ChipPlugin;

public class UseItemPacketAdapter extends PacketAdapter {

	public UseItemPacketAdapter(JavaPlugin plugin) {
		super(plugin, PacketType.Play.Client.USE_ITEM);
	}

	@Override
	public void onPacketReceiving(PacketEvent event) {
		final Player player = event.getPlayer();
		final PacketContainer packet = event.getPacket();

		if (packet.getType() != PacketType.Play.Client.USE_ITEM) {
			return;
		}

		if (player.hasPermission(ChipPlugin.PERMISSION_BYPASS)) {
			return;
		}

		for (ItemStack itemStack : packet.getItemModifier().getValues()) {
			if (itemStack == null) {
				continue;
			}
			
			if (itemStack.getType() == Material.AIR) {
				continue;
			}
			
			if (ChipPlugin.hasModifications(itemStack)) {
				ChipPlugin.notifyItemStackUsed(Optional.of(player.getName()), itemStack);
				event.setCancelled(true);
			}
		}
	}

}
