package com.ruinscraft.chip;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
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
import com.ruinscraft.chip.packetadapters.ChunkDataPacketAdapter;
import com.ruinscraft.chip.packetadapters.HeldItemChangePacketAdapter;
import com.ruinscraft.chip.packetadapters.SetCreativeSlotPacketAdapter;
import com.ruinscraft.chip.packetadapters.SpawnEntityPacketAdapter;
import com.ruinscraft.chip.packetadapters.UseItemPacketAdapter;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

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
	public boolean customPotions;
	public boolean attributeModifiers;
	public boolean deathLootTable;
	public boolean size;
	public boolean entityTag;
	public boolean explosionRadius;
	public boolean tileEntityData;
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

		saveDefaultConfig();

		getConfig().options().copyDefaults(true);
		saveConfig();
		
		loadConfigValues();

		PluginManager pluginManager = getServer().getPluginManager();

		if (pluginManager.getPlugin("ProtocolLib") == null) {
			getLogger().log(Level.WARNING, "ProtocolLib not found");
			pluginManager.disablePlugin(this);
			return;
		}

		// initialize cache
		checkerCache = CacheBuilder.newBuilder().expireAfterAccess(15, TimeUnit.MINUTES).maximumSize(15000).build(new CheckerCacheLoader());

		// initialize checkers
		itemStackChecker = new ItemStackChecker();
		entityChecker = new EntityChecker();

		// initialize fixers
		itemStackFixer = new ItemStackFixer();
		entityFixer = new EntityFixer();

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
		pluginManager.registerEvents(new WorldListener(), this);
		
		if (enableEnvBlocking) {
			pluginManager.registerEvents(new BlockPhysicsListener(), this);
		}

		getCommand("chip").setExecutor(this);
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
					reload(sender);
				});

				return true;
			}
		}

		sender.sendMessage(COLOR_BASE + info);

		if (useChunkData) {
			sender.sendMessage(COLOR_BASE + "Chunk load inspection is currently on. This will cause lag. You can disable this in the CHIP configuration file.");
		}

		return true;
	}
	
	public void reload(CommandSender sender) {
		long start = System.currentTimeMillis();

		String info = getDescription().getFullName();
		
		String reloading = "Reloading config for " + info;

		if (sender != null && sender != getServer().getConsoleSender()) {
			sender.sendMessage(COLOR_BASE + reloading);	
		}

		getLogger().log(Level.INFO, reloading);

		// clear cache
		checkerCache.invalidateAll();

		// reload config
		saveDefaultConfig();
		getConfig().options().copyDefaults(true);
		saveConfig();
		reloadConfig();
		loadConfigValues();

		ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
		
		if (useChunkData) {
			protocolManager.addPacketListener(new ChunkDataPacketAdapter(this));
			getLogger().log(Level.INFO, "Using chunk load inspection (experimental). This checks inventory holding blocks in loading chunks. Expect lag from this feature.");
		}
		
		PluginManager pluginManager = getServer().getPluginManager();

		if (enableEnvBlocking) {
			pluginManager.registerEvents(new BlockPhysicsListener(), this);
		}
		
		long time = System.currentTimeMillis() - start;

		String finished = "Finished reload in " + time + "ms";
		
		if (sender != null && sender != getServer().getConsoleSender()) {
			sender.sendMessage(COLOR_BASE + finished);
		}
		
		getLogger().log(Level.INFO, finished);
	}

	public void loadConfigValues() {
		useChunkData = getConfig().getBoolean("chunk_load_inspection");
		removeItem = getConfig().getBoolean("remove_item");
		removeEntity = getConfig().getBoolean("remove_entity");
		opsBypassChecks = getConfig().getBoolean("ops_bypass_checks");
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
		customPotions = getConfig().getBoolean("allowed_modifications.nbt.custom_potions");
		attributeModifiers = getConfig().getBoolean("allowed_modifications.nbt.attribute_modifiers");
		deathLootTable = getConfig().getBoolean("allowed_modifications.nbt.death_loot_table");
		size = getConfig().getBoolean("allowed_modifications.nbt.size");
		entityTag = getConfig().getBoolean("allowed_modifications.nbt.entity_tag");
		explosionRadius = getConfig().getBoolean("allowed_modifications.nbt.explosion_radius");
		tileEntityData = getConfig().getBoolean("allowed_modifications.nbt.tile_entity_data");
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

	public static Set<Modification> getModifications(Object o) {
		if (o instanceof Entity) {
			// entities can't be cached
			return getInstance().getEntityChecker().getModifications((Entity) o);
		}

		return getInstance().getCheckerCache().getUnchecked(o);
	}

	public static List<String> getPrettyModifications(Object o) {
		return getModifications(o).stream().map(Modification::getPretty).collect(Collectors.toList());
	}

	public static boolean hasModifications(Object o) {
		return !getModifications(o).isEmpty();
	}

	public static void fixItemStack(ItemStack itemStack) {
		getInstance().getItemStackFixer().fix(itemStack);
	}

	public static void fixEntity(Entity entity) {
		getInstance().getEntityFixer().fix(entity);
	}

	/**
	 * Clean an {@link org.bukkit.inventory.Inventory} of modified items.
	 * 
	 * If the description of the inventory is the username of an 
	 * online {@link org.bukkit.entity.Player}, it will clean their Enderchest.
	 * 
	 * @param description
	 * @param inventory
	 */
	public static void cleanInventory(Optional<String> description, Inventory inventory) {
		try {
			Player player = Bukkit.getPlayer(description.get());

			if (player.hasPermission(PERMISSION_BYPASS)) {
				return;
			}

			if (getInstance().opsBypassChecks && player.isOp()) {
				return;
			}

			// this will clean the user's enderchest if they don't have bypass permission
			// it won't run again because "<player>'s Enderchest" is not a player
			cleanInventory(Optional.of(player.getName() + "'s Enderchest"), player.getEnderChest());
		} catch (Exception e) {
			// do nothing
		}

		for (ItemStack itemStack : inventory.getContents()) {
			if (itemStack == null) {
				continue;
			}

			if (itemStack.getType() == Material.AIR) {
				continue;
			}

			if (hasModifications(itemStack)) {
				TextComponent message = new TextComponent(description.orElse("?") + " had modified " + itemStack.getType().name() + " (hover for info)");

				message.setColor(COLOR_BASE);

				message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(String.join(", ", getPrettyModifications(itemStack))).create()));

				notify(message);

				if (getInstance().removeItem) {
					inventory.remove(itemStack);
				} else {
					getInstance().getItemStackFixer().fix(itemStack);
				}
			}
		}
	}

	/**
	 * Clean an {@link org.bukkit.entity.Entity} of modifications.
	 * 
	 * @param description
	 * @param entity
	 */
	public static void cleanEntity(Optional<String> description, Entity entity) {
		if (entity == null) {
			return;
		}

		if (hasModifications(entity)) {
			TextComponent message = new TextComponent(entity.getType().name() + " had modifications (hover for info)");

			message.setColor(COLOR_BASE);

			message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(String.join(", ", getPrettyModifications(entity))).create()));

			notify(message);

			if (getInstance().removeEntity) {
				entity.remove();
			} else {
				getInstance().getEntityFixer().fix(entity);
			}
		}
	}

	/**
	 * Notify all online Players and console with the notify permission of a message
	 * 
	 * @param message
	 */
	public static void notify(BaseComponent message) {
		if (ChipPlugin.getInstance().chatNotifications) {
			Bukkit.getOnlinePlayers().forEach(p -> {
				if (p.hasPermission(PERMISSION_NOTIFY)) p.spigot().sendMessage(message);
			});
		}

		if (ChipPlugin.getInstance().consoleNotifications) {
			getInstance().getLogger().log(Level.INFO, message.toPlainText());
		}
	}

	public static void notifyItemStackCreated(Optional<String> description, ItemStack itemStack) {
		TextComponent message = new TextComponent(description.orElse("?") + " spawned " + itemStack.getType().name() + " which had modifications (hover for info)");

		message.setColor(COLOR_BASE);

		message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(String.join(", ", getPrettyModifications(itemStack))).create()));

		notify(message);
	}

	public static void notifyItemStackUsed(Optional<String> description, ItemStack itemStack) {
		TextComponent message = new TextComponent(description.orElse("?") + " used " + itemStack.getType().name() + " which had modifications (hover for info)");

		message.setColor(COLOR_BASE);

		message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(String.join(", ", getPrettyModifications(itemStack))).create()));

		notify(message);
	}

}
