package com.ruinscraft.chip;

import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.ruinscraft.chip.checkers.CheckerCacheLoader;
import com.ruinscraft.chip.listeners.PlayerListener;
import com.ruinscraft.chip.packetadapters.ChunkDataPacketAdapter;
import com.ruinscraft.chip.packetadapters.HeldItemChangePacketAdapter;
import com.ruinscraft.chip.packetadapters.SetCreativeSlotPacketAdapter;
import com.ruinscraft.chip.packetadapters.SpawnEntityPacketAdapter;
import com.ruinscraft.chip.packetadapters.UseItemPacketAdapter;

public class ChipPlugin extends JavaPlugin implements CommandExecutor {

	// colors
	public final ChatColor COLOR_ERROR = ChatColor.RED;
	public final ChatColor COLOR_BASE = ChatColor.YELLOW;
	
	// non-command permissions
	public static final String PERMISSION_BYPASS = "chip.bypass";
	public static final String PERMISSION_NOTIFY = "chip.notify";
	
	// configuration options
	public final boolean useChunkData = getConfig().getBoolean("chunk_load_inspection");
	public final boolean removeItem = getConfig().getBoolean("remove_item");
	public final boolean removeEntity = getConfig().getBoolean("remove_entity");
	public final boolean opsBypassChecks = getConfig().getBoolean("ops_bypass_checks");
	public final boolean chatNotifications = getConfig().getBoolean("notifications.chat");
	public final boolean consoleNotifications = getConfig().getBoolean("notifications.console");
	public final boolean aboveNormalEnchants = getConfig().getBoolean("allowed_modifications.enchantments.above_normal_enchants");
	public final boolean belowNormalEnchants = getConfig().getBoolean("allowed_modifications.enchantments.below_normal_enchants");
	public final boolean conflictingEnchants = getConfig().getBoolean("allowed_modifications.enchantments.conflicting_enchants");
	public final boolean nonCraftableFireworks = getConfig().getBoolean("allowed_modifications.fireworks.non_craftable_fireworks");
	public final boolean customLore = getConfig().getBoolean("allowed_modifications.item_meta.custom_lore");
	public final boolean unbreakableItems = getConfig().getBoolean("allowed_modifications.item_meta.unbreakable_items");
	public final boolean coloredCustomNames = getConfig().getBoolean("allowed_modifications.item_meta.colored_custom_names");
	public final boolean coloredCustomLore = getConfig().getBoolean("allowed_modifications.item_meta.colored_custom_lore");
	public final boolean invulnerable = getConfig().getBoolean("allowed_modifications.entities.invulnerable");
	public final boolean glowing = getConfig().getBoolean("allowed_modifications.entities.glowing");
	public final boolean customNameVisible = getConfig().getBoolean("allowed_modifications.entities.custom_name_visible");
	public final boolean smallArmorStands = getConfig().getBoolean("allowed_modifications.entities.armor_stands.small");
	public final boolean visibleArmorStands = getConfig().getBoolean("allowed_modifications.entities.armor_stands.visible");
	public final boolean basePlateArmorStands = getConfig().getBoolean("allowed_modifications.entities.armor_stands.base_plate");
	public final boolean customPotions = getConfig().getBoolean("allowed_modifications.potions.custom_potions");
	public final int maxCustomNameLength = getConfig().getInt("max_custom_name_length");
	public final int maxCustomLoreLength = getConfig().getInt("max_custom_lore_length");
	
	private final LoadingCache<Object, Set<Modification>> checkerCache = CacheBuilder.newBuilder().build(new CheckerCacheLoader());
	
	private static ChipPlugin instance;
	
	public static ChipPlugin getInstance() {
		return instance;
	}
	
	@Override
	public void onEnable() {
		instance = this;
		
		saveDefaultConfig();
		
		PluginManager pluginManager = getServer().getPluginManager();

		if (pluginManager.getPlugin("ProtocolLib") == null) {
			getLogger().log(Level.WARNING, "ProtocolLib not found");
			pluginManager.disablePlugin(this);
			return;
		}

		ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

		protocolManager.addPacketListener(new SetCreativeSlotPacketAdapter(this));
		protocolManager.addPacketListener(new SpawnEntityPacketAdapter(this));
		protocolManager.addPacketListener(new HeldItemChangePacketAdapter(this));
		protocolManager.addPacketListener(new UseItemPacketAdapter(this));
		
		if (useChunkData) {
			protocolManager.addPacketListener(new ChunkDataPacketAdapter(this));
			getLogger().log(Level.INFO, "Using chunk load inspection (experimental). This checks inventory holding blocks in loading chunks. Expect lag from this feature.");
		}

		pluginManager.registerEvents(new PlayerListener(), this);
		
		getCommand("chip").setExecutor(this);
	}

	@Override
	public void onDisable() {
		instance = null;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		final String info = getDescription().getFullName();

		sender.sendMessage(COLOR_BASE + info);

		if (useChunkData) {
			sender.sendMessage(COLOR_BASE + "Chunk load inspection is currently on. This will cause lag. You can disable this in the CHIP configuration file.");
		}
		
		return true;
	}
	
	public static Set<Modification> getModifications(Object object) {
		return getInstance().checkerCache.getUnchecked(object);
	}
	
	public static boolean hasModifications(Object object) {
		return !getModifications(object).isEmpty();
	}
	
	public static void cleanInventory(Optional<String> description, Inventory inventory) {
		
	}
	
	public static void cleanEntity(Optional<String> description, Entity entity) {
		
	}
	
	public static void notify(String message) {
		Bukkit.getOnlinePlayers().forEach(p -> {
			if (p.hasPermission(PERMISSION_NOTIFY)) p.sendMessage(message);
		});
	}

}