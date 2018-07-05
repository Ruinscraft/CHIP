package com.ruinscraft.chip;

import java.lang.reflect.Method;
import java.util.Arrays;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.utility.MinecraftMethods;

import io.netty.buffer.ByteBuf;

public class NettyUtil {

	// reimplemented from ProtocolLib
	public static byte[] getBytes(ByteBuf buffer) {
		byte[] array = new byte[buffer.readableBytes()];
		
		buffer.readBytes(array);
		
		return array;
	}

	// reimplemented from ProtocolLib
	public static byte[] bytesFromPacket(PacketContainer packet) {
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
