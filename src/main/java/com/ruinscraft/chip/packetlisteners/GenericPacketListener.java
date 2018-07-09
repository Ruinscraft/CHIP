package com.ruinscraft.chip.packetlisteners;

import static com.ruinscraft.chip.util.NettyUtil.*;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.events.ListeningWhitelist;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.injector.packet.PacketRegistry;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.ruinscraft.chip.ChipPlugin;
import com.ruinscraft.chip.util.ChipUtil;

public class GenericPacketListener implements PacketListener {

	private static final ChipPlugin chip = ChipPlugin.getInstance();
	
	// from decompiled Minecraft 1.12.2 client
	private static final int MAX_PACKET_SIZE = 2097152;

	@Override
	public void onPacketSending(PacketEvent event) {
		check(event);
	}

	@Override
	public void onPacketReceiving(PacketEvent event) {
		check(event);
	}
	
	private static void check(PacketEvent event) {
		Player player = event.getPlayer();
		PacketContainer packet = event.getPacket();
		
		if (packet == null) {
			return;
		}
		
		if (event.isAsync()) {
			return;
		}
		
		// return if temporary player
		try {
			player.getWorld();
		} catch (Exception e) {
			return;
		}
		
		boolean useWorldWhitelist = chip.useWorldWhitelist;
		boolean currentWorldIsWhitelisted = chip.whitelistedWorlds.contains(player.getWorld().getName());
		
		if (useWorldWhitelist && currentWorldIsWhitelisted) {
			return;
		}

		ItemStack possibleItemStack = packet.getItemModifier().readSafely(0);
		Entity possibleEntity = packet.getEntityModifier(event).readSafely(0);
		BlockPosition possibleBlockPosition = packet.getBlockPositionModifier().readSafely(0);
		
		if (bytesFromPacket(packet).length > MAX_PACKET_SIZE) {
			event.setCancelled(true);

			if (possibleItemStack != null) {
				player.getInventory().remove(possibleItemStack);
			}
			
			if (possibleEntity != null) {
				possibleEntity.remove();
			}

			ChipPlugin.getInstance().getLogger().info("Oversized packet involving " + player.getName() + " was dropped.");

			return;
		}

		if (player.getItemInHand() != null && player.getItemInHand().getType() != Material.AIR) {
			ChipUtil.fix(player.getItemInHand(), player);
		}
		
		if (possibleBlockPosition != null) {
			Block block = player.getWorld().getBlockAt(possibleBlockPosition.getX(), possibleBlockPosition.getY(), possibleBlockPosition.getZ());
			
			if (block.getState() instanceof InventoryHolder) {
				InventoryHolder inventoryHolder = (InventoryHolder) block.getState();
				
				for (ItemStack itemStack : inventoryHolder.getInventory().getContents()) {
					if (itemStack == null) {
						continue;
					}
					
					if (itemStack.getType() == Material.AIR) {
						continue;
					}
					
					ChipUtil.fix(itemStack, block);
				}
			}
			
			return;
		}
		
		if (possibleItemStack != null) {
			ChipUtil.fix(possibleItemStack, player);
			
			return;
		}
		
		if (possibleEntity != null) {
			if (possibleEntity instanceof Item) {
				Item item = (Item) possibleEntity;
				
				ChipUtil.fix(item.getItemStack(), player);
			} else {
				ChipUtil.fix(possibleEntity, player);
			}
			
			return;
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
