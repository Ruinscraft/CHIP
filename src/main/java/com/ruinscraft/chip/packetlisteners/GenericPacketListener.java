package com.ruinscraft.chip.packetlisteners;

import java.util.Optional;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.injector.netty.WirePacket;
import com.comphenix.protocol.injector.packet.PacketRegistry;
import com.ruinscraft.chip.ChipPlugin;
import com.ruinscraft.chip.ChipUtil;

public class GenericPacketListener implements PacketListener {

	// from decompiled Minecraft 1.12.2 client
	private static final int PACKET_BYTES_LIMIT = 2097152;

	@Override
	public void onPacketSending(PacketEvent event) {
		Player player = event.getPlayer();
		PacketContainer packet = event.getPacket();
		Object potentialItemStack = packet.getItemModifier().readSafely(0);
		int packetSize = WirePacket.bytesFromPacket(packet).length;
		
		if (packetSize > PACKET_BYTES_LIMIT) {
			event.setCancelled(true);

			if (potentialItemStack instanceof ItemStack) {
				ItemStack itemStack = (ItemStack) potentialItemStack;

				// try to remove the offending item from the player's inventory
				player.getInventory().remove(itemStack);
			}

			ChipPlugin.getInstance().getLogger().info("Cancelled a packet (" + packet.getType() +  ") which was too large originating from server to player: " + player.getName());
			
			return;
		}

		if (potentialItemStack instanceof ItemStack) {
			ItemStack itemStack = (ItemStack) potentialItemStack;

			ChipUtil.fix(player.getLocation().getWorld().getName(), itemStack, Optional.of(player.getName()), Optional.of(ChipUtil.getLocationString(player.getLocation())), Optional.of(player.getInventory()));
		}
	}

	@Override
	public void onPacketReceiving(PacketEvent event) {
		Player player = event.getPlayer();
		PacketContainer packet = event.getPacket();
		Object potentialItemStack = packet.getItemModifier().readSafely(0);
		int packetSize = WirePacket.bytesFromPacket(packet).length;
		
		if (packetSize > PACKET_BYTES_LIMIT) {
			event.setCancelled(true);

			if (potentialItemStack instanceof ItemStack) {
				ItemStack itemStack = (ItemStack) potentialItemStack;

				// try to remove the offending item from the player's inventory
				player.getInventory().remove(itemStack);
			}

			ChipPlugin.getInstance().getLogger().info("Cancelled a packet (" + packet.getType() +  ") which was too large originating from player: " + player.getName());
			
			return;
		}

		if (potentialItemStack instanceof ItemStack) {
			ItemStack itemStack = (ItemStack) potentialItemStack;

			ChipUtil.fix(player.getLocation().getWorld().getName(), itemStack, Optional.of(player.getName()), Optional.of(ChipUtil.getLocationString(player.getLocation())), Optional.of(player.getInventory()));
		}
	}

	@Override
	public ListeningWhitelist getSendingWhitelist() {
		return ListeningWhitelist.newBuilder().high().types(PacketRegistry.getServerPacketTypes()).gamePhaseBoth().build();
	}

	@Override
	public ListeningWhitelist getReceivingWhitelist() {
		return ListeningWhitelist.newBuilder().high().types(PacketRegistry.getClientPacketTypes()).gamePhaseBoth().build();
	}

	@Override
	public Plugin getPlugin() {
		return ChipPlugin.getInstance();
	}

}
