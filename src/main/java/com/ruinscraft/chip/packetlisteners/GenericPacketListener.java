package com.ruinscraft.chip.packetlisteners;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.injector.netty.WirePacket;
import com.comphenix.protocol.injector.packet.PacketRegistry;
import com.comphenix.protocol.utility.MinecraftMethods;
import com.ruinscraft.chip.ChipPlugin;
import com.ruinscraft.chip.ChipUtil;

import io.netty.buffer.ByteBuf;

public class GenericPacketListener implements PacketListener {

	@Override
	public void onPacketSending(PacketEvent event) {
		Player player = event.getPlayer();
		PacketContainer packet = event.getPacket();
		ItemStack possibleItemStack = packet.getItemModifier().readSafely(0);

		if (bytesFromPacket(packet).length > 2097152) {
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
		ItemStack possibleItemStack = packet.getItemModifier().readSafely(0);

		if (WirePacket.bytesFromPacket(packet).length > 2097152) {
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

	// reimplemented from ProtocolLib
	private static byte[] getBytes(ByteBuf buffer) {
		byte[] array = new byte[buffer.readableBytes()];
		buffer.readBytes(array);
		return array;
	}

	// reimplemented from ProtocolLib
	private static byte[] bytesFromPacket(PacketContainer packet) {
		ByteBuf buffer = PacketContainer.createPacketBuffer();
		ByteBuf store = PacketContainer.createPacketBuffer();

		// Read the bytes once
		Method write = MinecraftMethods.getPacketWriteByteBufMethod();

		try {
			write.invoke(packet.getHandle(), buffer);
		} catch (ReflectiveOperationException ex) {
			throw new RuntimeException("Failed to read packet contents.", ex);
		}

		byte[] bytes = getBytes(buffer);

		buffer.release();

		// Rewrite them to the packet to avoid issues with certain packets
		if (packet.getType() == PacketType.Play.Server.CUSTOM_PAYLOAD
				|| packet.getType() == PacketType.Play.Client.CUSTOM_PAYLOAD) {
			// Make a copy of the array before writing
			byte[] ret = Arrays.copyOf(bytes, bytes.length);
			store.writeBytes(bytes);

			Method read = MinecraftMethods.getPacketReadByteBufMethod();

			try {
				read.invoke(packet.getHandle(), store);
			} catch (ReflectiveOperationException ex) {
				throw new RuntimeException("Failed to rewrite packet contents.", ex);
			}

			return ret;
		}

		store.release();

		return bytes;
	}

}
