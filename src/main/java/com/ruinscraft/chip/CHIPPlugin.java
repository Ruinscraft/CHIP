package com.ruinscraft.chip;

import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.ruinscraft.chip.listeners.PlayerListener;
import com.ruinscraft.chip.packetadapters.ChunkDataPacketAdapter;
import com.ruinscraft.chip.packetadapters.HeldItemChangePacketAdapter;
import com.ruinscraft.chip.packetadapters.SetCreativeSlotPacketAdapter;
import com.ruinscraft.chip.packetadapters.SpawnEntityPacketAdapter;
import com.ruinscraft.chip.packetadapters.UseItemPacketAdapter;

public class CHIPPlugin extends JavaPlugin implements CommandExecutor {

	// colors
	public static final ChatColor COLOR_ERROR = ChatColor.RED;
	public static final ChatColor COLOR_BASE = ChatColor.YELLOW;
	
	// non-command permissions
	public static final String PERMISSION_BYPASS = "chip.bypass";
	public static final String PERMISSION_NOTIFY = "chip.notify";

	@Override
	public void onEnable() {
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
		
		boolean useChunkData = getConfig().getBoolean("chunk_load_inspection");
		
		if (useChunkData) {
			protocolManager.addPacketListener(new ChunkDataPacketAdapter(this));
			getLogger().log(Level.INFO, "Using chunk load inspection (experimental). This checks inventory holding blocks in loading chunks. Expect lag from this feature.");
		}

		pluginManager.registerEvents(new PlayerListener(), this);
		
		getCommand("chip").setExecutor(this);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		final String info = getDescription().getFullName();

		sender.sendMessage(COLOR_BASE + info);

		return true;
	}

}
