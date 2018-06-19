package com.ruinscraft.chip.packetadapters;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.ruinscraft.chip.ChipPlugin;
import com.ruinscraft.chip.ChipUtil;

import net.md_5.bungee.api.ChatColor;

public class SetCreativeSlotPacketAdapter extends PacketAdapter {

	private static final Map<Integer, Integer> SLOT_ID_MAPPINGS = new HashMap<>();
	
	static {
		SLOT_ID_MAPPINGS.put(0, 36);
		SLOT_ID_MAPPINGS.put(1, 37);
		SLOT_ID_MAPPINGS.put(2, 38);
		SLOT_ID_MAPPINGS.put(3, 39);
		SLOT_ID_MAPPINGS.put(4, 40);
		SLOT_ID_MAPPINGS.put(5, 41);
		SLOT_ID_MAPPINGS.put(6, 42);
		SLOT_ID_MAPPINGS.put(7, 43);
		SLOT_ID_MAPPINGS.put(8, 44);
	}
	
	public SetCreativeSlotPacketAdapter(JavaPlugin plugin) {
		super(plugin, PacketType.Play.Client.SET_CREATIVE_SLOT);
	}

	@Override
	public void onPacketReceiving(PacketEvent event) {
		final Player player = event.getPlayer();
		final PacketContainer packet = event.getPacket();

		int requestedSlot = packet.getIntegers().getValues().get(0);
		ItemStack requestedItemStack = packet.getItemModifier().getValues().get(0);

		if (requestedItemStack == null) {
			return;
		}
		
		// TODO: rework for all of hotbar not just item in hand
		// if the item which was requested to be changed is the one they are currently holding
		if (SLOT_ID_MAPPINGS.get(player.getInventory().getHeldItemSlot()) == requestedSlot) {
			
			if (ChipPlugin.getInstance().preventBookForgery && player.getItemInHand().getType() == Material.WRITTEN_BOOK && requestedItemStack.getType() == Material.WRITTEN_BOOK) {
				
				BookMeta currentBookMeta = (BookMeta) player.getItemInHand().getItemMeta();
				BookMeta newBookMeta = (BookMeta) requestedItemStack.getItemMeta();
				
				if (!currentBookMeta.getAuthor().equals(newBookMeta.getAuthor())) {
					player.sendMessage(ChatColor.RED + "You are not allowed to change the author of books.");
					event.setCancelled(true);
					return;
				}
			}
		}
		
		ChipUtil.fix(player.getLocation().getWorld().getName(), requestedItemStack, Optional.of(player.getName()), Optional.of(ChipUtil.getLocationString(player.getLocation())), Optional.of(player.getInventory()));
	}

}
