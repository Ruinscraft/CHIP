package com.ruinscraft.chip.packetadapters;

import java.util.Optional;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.ruinscraft.chip.CHIPPlugin;
import com.ruinscraft.chip.InvalidAttributeException;

public class SetCreativeSlotPacketAdapter extends PacketAdapter {

	private final JavaPlugin plugin;
	
	public SetCreativeSlotPacketAdapter(JavaPlugin plugin) {
		super(plugin, PacketType.Play.Client.SET_CREATIVE_SLOT);
		this.plugin = plugin;
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
				CHIPPlugin.getInstance().getUtil().checkItem(itemStack);
			} catch (InvalidAttributeException e) {
				event.setCancelled(true);
				
				if (player.getInventory().contains(itemStack)) {
					plugin.getServer().getScheduler().runTask(plugin, () -> {
						CHIPPlugin.getInstance().getUtil().cleanInventory(Optional.of(player.getName()), player.getInventory());
					});
				}
			}
		}
	}

}
