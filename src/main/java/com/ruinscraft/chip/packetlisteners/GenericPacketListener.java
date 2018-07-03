package com.ruinscraft.chip.packetlisteners;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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

import io.netty.buffer.ByteBuf;

public class GenericPacketListener implements PacketListener {

	private static final Map<UUID, Integer> LARGE_PACKET_OFFENDERS = new ConcurrentHashMap<>();
	
	// comes from decompiled Minecraft 1.12.2 client
	private static final int PACKET_BYTES_LIMIT = 2097152;
	
	@Override
	public void onPacketSending(PacketEvent event) {
		Player player = event.getPlayer();
		PacketContainer packet = event.getPacket();
		WirePacket wirePacket = WirePacket.fromPacket(packet);
		ByteBuf buffer = wirePacket.serialize();

		if (buffer.readableBytes() > PACKET_BYTES_LIMIT) {
			if (LARGE_PACKET_OFFENDERS.getOrDefault(player.getUniqueId(), 0) > 4) {
				player.getInventory().clear();
				
				LARGE_PACKET_OFFENDERS.remove(player.getUniqueId());
				
				ChipPlugin.getInstance().getLogger().info(player.getName() + " is recieving packets which are too large. Clearing their inventory...");
			}
			
			LARGE_PACKET_OFFENDERS.merge(player.getUniqueId(), 1, Integer::sum);
			
			ChipPlugin.getInstance().getLogger().info("Cancelled a packet (" + packet.getType() +  ") which was too large originating from the server sending to: " + player.getName());
			
			event.setCancelled(true);
		}
		
		try {
			ItemStack itemStack = packet.getItemModifier().getValues().get(0);
			
			ChipUtil.fix(player.getLocation().getWorld().getName(), itemStack, Optional.of(player.getName()), Optional.of(ChipUtil.getLocationString(player.getLocation())), Optional.of(player.getInventory()));
		// packet did not contain itemstack
		} catch (Exception e) {}
	}

	@Override
	public void onPacketReceiving(PacketEvent event) {
		Player player = event.getPlayer();
		PacketContainer packet = event.getPacket();
		WirePacket wirePacket = WirePacket.fromPacket(packet);
		ByteBuf buffer = wirePacket.serialize();

		if (buffer.readableBytes() > PACKET_BYTES_LIMIT) {
			if (LARGE_PACKET_OFFENDERS.getOrDefault(player.getUniqueId(), 0) > 4) {
				player.getInventory().clear();

				LARGE_PACKET_OFFENDERS.remove(player.getUniqueId());
				
				ChipPlugin.getInstance().getLogger().info(player.getName() + " is sending packets which are too large. Clearing their inventory...");
			}
			
			LARGE_PACKET_OFFENDERS.merge(player.getUniqueId(), 1, Integer::sum);
			
			ChipPlugin.getInstance().getLogger().info("Cancelled a packet (" + packet.getType() +  ") which was too large originating from player: " + player.getName());
			
			event.setCancelled(true);
		}
		
		try {
			ItemStack itemStack = packet.getItemModifier().getValues().get(0);
			
			ChipUtil.fix(player.getLocation().getWorld().getName(), itemStack, Optional.of(player.getName()), Optional.of(ChipUtil.getLocationString(player.getLocation())), Optional.of(player.getInventory()));
		// packet did not contain itemstack
		} catch (Exception e) {}
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
