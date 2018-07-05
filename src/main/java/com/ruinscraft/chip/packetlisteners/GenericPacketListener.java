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

import static com.ruinscraft.chip.NettyUtil.*;

public class GenericPacketListener implements PacketListener {

	// from decompiled Minecraft 1.12.2 client
	private static final int MAX_PACKET_SIZE = 2097152;

	@Override
	public void onPacketSending(PacketEvent event) {
		Player player = event.getPlayer();
		PacketContainer packet = event.getPacket();

		if (packet == null) {
			return;
		}

		ItemStack possibleItemStack = packet.getItemModifier().readSafely(0);

		if (bytesFromPacket(packet).length > MAX_PACKET_SIZE) {
			event.setCancelled(true);

			if (possibleItemStack != null) {
				player.getInventory().remove(possibleItemStack);
			}

			ChipPlugin.getInstance().getLogger().info("Oversized packet to " + player.getName() + " was dropped.");

			return;
		}

		if (possibleItemStack != null) {
			ChipUtil.fix(player.getLocation().getWorld().getName(), possibleItemStack, Optional.of(player.getName()), Optional.of(ChipUtil.getLocationString(player.getLocation())), Optional.of(player.getInventory()));
		}
	}

	@Override
	public void onPacketReceiving(PacketEvent event) {
		Player player = event.getPlayer();
		PacketContainer packet = event.getPacket();

		if (packet == null) {
			return;
		}

		ItemStack possibleItemStack = packet.getItemModifier().readSafely(0);

		if (WirePacket.bytesFromPacket(packet).length > MAX_PACKET_SIZE) {
			event.setCancelled(true);

			if (possibleItemStack != null) {
				player.getInventory().remove(possibleItemStack);
			}

			ChipPlugin.getInstance().getLogger().info("Oversized packet from " + player.getName() + " was dropped.");

			return;
		}

		if (possibleItemStack != null) {
			ChipUtil.fix(player.getLocation().getWorld().getName(), possibleItemStack, Optional.of(player.getName()), Optional.of(ChipUtil.getLocationString(player.getLocation())), Optional.of(player.getInventory()));
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
