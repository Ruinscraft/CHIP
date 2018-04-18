package com.ruinscraft.chip;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.ruinscraft.chip.checkers.Checker;
import com.ruinscraft.chip.checkers.CheckerCacheLoader;
import com.ruinscraft.chip.checkers.EntityChecker;
import com.ruinscraft.chip.checkers.ItemStackChecker;
import com.ruinscraft.chip.fixers.EntityFixer;
import com.ruinscraft.chip.fixers.Fixer;
import com.ruinscraft.chip.fixers.ItemStackFixer;
import com.ruinscraft.chip.listeners.BlockPhysicsListener;
import com.ruinscraft.chip.listeners.PlayerListener;
import com.ruinscraft.chip.listeners.WorldListener;
import com.ruinscraft.chip.packetadapters.HeldItemChangePacketAdapter;
import com.ruinscraft.chip.packetadapters.SetCreativeSlotPacketAdapter;
import com.ruinscraft.chip.packetadapters.UseItemPacketAdapter;

import net.md_5.bungee.api.ChatColor;

public class ChipPlugin extends JavaPlugin implements CommandExecutor {

	// colors
	public static final ChatColor COLOR_ERROR = ChatColor.RED;
	public static final ChatColor COLOR_BASE = ChatColor.YELLOW;

	// non-command permissions
	public static final String PERMISSION_BYPASS = "chip.bypass";
	public static final String PERMISSION_NOTIFY = "chip.notify";

	// configuration options
	public boolean useChunkData;
	public boolean removeItem;
	public boolean removeEntity;
	public boolean opsBypassChecks;
	public boolean useWorldWhitelist;
	public List<String> whitelistedWorlds;
	public boolean chatNotifications;
	public boolean consoleNotifications;
	public boolean aboveNormalEnchants;
	public boolean belowNormalEnchants;
	public boolean conflictingEnchants;
	public boolean nonCraftableFireworks;
	public boolean customLore;
	public boolean unbreakableItems;
	public boolean coloredCustomNames;
	public boolean coloredCustomLore;
	public boolean invulnerable;
	public boolean glowing;
	public boolean customNameVisible;
	public boolean smallArmorStands;
	public boolean visibleArmorStands;
	public boolean basePlateArmorStands;
	public boolean noArms;
	public boolean customPotions;
	public boolean attributeModifiers;
	public boolean deathLootTable;
	public boolean size;
	public boolean entityTag;
	public boolean explosionRadius;
	public boolean tileEntityData;
	public boolean blockEntityTag;
	public int maxCustomNameLength;
	public int maxCustomLoreLength;
	public boolean ignoreHeadNames;
	public boolean ignoreHeadLores;
	public boolean enableEnvBlocking;
	public boolean notifyConsoleWhenCancelled;
	public boolean waterFlow;
	public boolean lavaFlow;
	public boolean tntUpdate;
	public boolean spongeUpdate;

	private LoadingCache<Object, Set<Modification>> checkerCache;

	private Checker<ItemStack> itemStackChecker;
	private Checker<Entity> entityChecker;
	private Fixer<ItemStack> itemStackFixer;
	private Fixer<Entity> entityFixer;

	private static ChipPlugin instance;

	public static ChipPlugin getInstance() {
		return instance;
	}

	@Override
	public void onEnable() {
		instance = this;

		load(getServer().getPluginManager(), ProtocolLibrary.getProtocolManager());
	}

	@Override
	public void onDisable() {
		instance = null;

		checkerCache.invalidateAll();
		checkerCache = null;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		String info = getDescription().getFullName();

		if (args.length > 0) {
			if (args[0].equalsIgnoreCase("reload")) {
				getServer().getScheduler().runTaskAsynchronously(this, () -> {
					reload(sender, getServer().getPluginManager(), ProtocolLibrary.getProtocolManager());
				});

				return true;
			}
		}

		sender.sendMessage(COLOR_BASE + info);
		
		if (useWorldWhitelist) {
			sender.sendMessage(COLOR_BASE + "CHIP is currently checking for modifications in the following worlds:");
			whitelistedWorlds.forEach(world -> sender.sendMessage(COLOR_BASE + "- " + world));
		}

		return true;
	}

	public synchronized void load(PluginManager pluginManager, ProtocolManager protocolManager) {
		// CHIP depends on ProtocolLib
		if (pluginManager.getPlugin("ProtocolLib") == null) {
			getLogger().log(Level.WARNING, "ProtocolLib not found");
			pluginManager.disablePlugin(this);
			return;
		}

		loadConfig();

		// load all the config vars in from the config
		loadConfigValues();

		// initialize Guava LoadingCache
		checkerCache = CacheBuilder.newBuilder().expireAfterAccess(15, TimeUnit.MINUTES).maximumSize(15000).build(new CheckerCacheLoader());

		// initialize checkers
		itemStackChecker = new ItemStackChecker();
		entityChecker = new EntityChecker();

		// initialize fixers
		itemStackFixer = new ItemStackFixer();
		entityFixer = new EntityFixer();

		// add packet listeners for ProtocolLib
		protocolManager.addPacketListener(new SetCreativeSlotPacketAdapter(this));
		protocolManager.addPacketListener(new HeldItemChangePacketAdapter(this));
		protocolManager.addPacketListener(new UseItemPacketAdapter(this));

		// add bukkit listeners
		pluginManager.registerEvents(new PlayerListener(), this);
		pluginManager.registerEvents(new WorldListener(), this);

		if (enableEnvBlocking) {
			pluginManager.registerEvents(new BlockPhysicsListener(), this);
		}

		// add bukkit CommandExecutors
		getCommand("chip").setExecutor(this);

		getLogger().info("Enabled " + getDescription().getFullName());
	}

	public synchronized void reload(CommandSender sender, PluginManager pluginManager, ProtocolManager protocolManager) {
		// log time
		long start = System.currentTimeMillis();
		String reloading = getDescription().getFullName() + " > reloading config...";
		if (sender != null && sender != getServer().getConsoleSender()) {
			sender.sendMessage(COLOR_BASE + reloading);
		}
		getLogger().info(reloading);

		// clear cache
		checkerCache.invalidateAll();

		// reload in case something changed
		reloadConfig();

		loadConfig();

		// load all the config vars in from the config
		loadConfigValues();

		if (enableEnvBlocking) {
			pluginManager.registerEvents(new BlockPhysicsListener(), this);
		}

		// log time
		long time = System.currentTimeMillis() - start;
		String finished = "Finished reload in " + time + "ms";
		if (sender != null && sender != getServer().getConsoleSender()) {
			sender.sendMessage(COLOR_BASE + finished);
		}
		getLogger().info(finished);
	}

	private void loadConfig() {
		// copy config from resources to local disk
		saveDefaultConfig();

		// copy any options which aren't in the existing config
		getConfig().options().copyDefaults(true);

		// save the config in case any keys didn't exist
		saveConfig();
	}

	public void loadConfigValues() {
		useChunkData = getConfig().getBoolean("chunk_load_inspection");
		removeItem = getConfig().getBoolean("remove_item");
		removeEntity = getConfig().getBoolean("remove_entity");
		opsBypassChecks = getConfig().getBoolean("ops_bypass_checks");
		useWorldWhitelist = getConfig().getBoolean("world_whitelist.use");
		whitelistedWorlds = getConfig().getStringList("world_whitelist.worlds");
		chatNotifications = getConfig().getBoolean("notifications.chat");
		consoleNotifications = getConfig().getBoolean("notifications.console");
		aboveNormalEnchants = getConfig().getBoolean("allowed_modifications.enchantments.above_normal_enchants");
		belowNormalEnchants = getConfig().getBoolean("allowed_modifications.enchantments.below_normal_enchants");
		conflictingEnchants = getConfig().getBoolean("allowed_modifications.enchantments.conflicting_enchants");
		nonCraftableFireworks = getConfig().getBoolean("allowed_modifications.fireworks.non_craftable_fireworks");
		customLore = getConfig().getBoolean("allowed_modifications.item_meta.custom_lore");
		unbreakableItems = getConfig().getBoolean("allowed_modifications.item_meta.unbreakable_items");
		coloredCustomNames = getConfig().getBoolean("allowed_modifications.item_meta.colored_custom_names");
		coloredCustomLore = getConfig().getBoolean("allowed_modifications.item_meta.colored_custom_lore");
		invulnerable = getConfig().getBoolean("allowed_modifications.entities.invulnerable");
		glowing = getConfig().getBoolean("allowed_modifications.entities.glowing");
		customNameVisible = getConfig().getBoolean("allowed_modifications.entities.custom_name_visible");
		smallArmorStands = getConfig().getBoolean("allowed_modifications.entities.armor_stands.small");
		visibleArmorStands = getConfig().getBoolean("allowed_modifications.entities.armor_stands.visible");
		basePlateArmorStands = getConfig().getBoolean("allowed_modifications.entities.armor_stands.base_plate");
		noArms = getConfig().getBoolean("allowed_modifications.entities.armor_stands.base_plate.no_arms");
		customPotions = getConfig().getBoolean("allowed_modifications.nbt.custom_potions");
		attributeModifiers = getConfig().getBoolean("allowed_modifications.nbt.attribute_modifiers");
		deathLootTable = getConfig().getBoolean("allowed_modifications.nbt.death_loot_table");
		size = getConfig().getBoolean("allowed_modifications.nbt.size");
		entityTag = getConfig().getBoolean("allowed_modifications.nbt.entity_tag");
		explosionRadius = getConfig().getBoolean("allowed_modifications.nbt.explosion_radius");
		tileEntityData = getConfig().getBoolean("allowed_modifications.nbt.tile_entity_data");
		blockEntityTag = getConfig().getBoolean("allowed_modifications.nbt.block_entity_tag");
		maxCustomNameLength = getConfig().getInt("max_custom_name_length");
		maxCustomLoreLength = getConfig().getInt("max_custom_lore_length_per_line");
		ignoreHeadNames = getConfig().getBoolean("ignore_head_names");
		ignoreHeadLores = getConfig().getBoolean("ignore_head_lores");
		enableEnvBlocking = getConfig().getBoolean("env_blocking.enable_env_blocking");
		notifyConsoleWhenCancelled = getConfig().getBoolean("env_blocking.notify_console_when_cancelled");
		waterFlow = getConfig().getBoolean("env_blocking.water_flow");
		lavaFlow = getConfig().getBoolean("env_blocking.lava_flow");
		tntUpdate = getConfig().getBoolean("env_blocking.tnt_update");
		spongeUpdate = getConfig().getBoolean("env_blocking.sponge_update");
	}

	public LoadingCache<Object, Set<Modification>> getCheckerCache() {
		return checkerCache;
	}

	public Checker<ItemStack> getItemStackChecker() {
		return itemStackChecker;
	}

	public Checker<Entity> getEntityChecker() {
		return entityChecker;
	}

	public Fixer<ItemStack> getItemStackFixer() {
		return itemStackFixer;
	}

	public Fixer<Entity> getEntityFixer() {
		return entityFixer;
	}

}
