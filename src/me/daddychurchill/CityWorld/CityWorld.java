package me.daddychurchill.CityWorld;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import me.daddychurchill.CityWorld.Plugins.LootProvider;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

public class CityWorld extends JavaPlugin implements CityWorldLog, Listener {

	public final static Logger log = Logger.getLogger("Minecraft.CityWorld");

	private final Map<String, CityWorldGenerator> generatorList = Maps.newHashMap();

	@Override
	public ChunkGenerator getDefaultWorldGenerator(String name, String style) {
		CityWorldGenerator generator = new CityWorldGenerator(this, name, style);

		this.generatorList.put(name.toLowerCase(), generator);

		return this.generatorList.get(name.toLowerCase());
	}

	@Override
	public void onDisable() {
		// remember for the next time
		saveConfig();

		// tell the world we are out of here
		reportMessage("Disabled");
	}

	private CityWorldSettings defaults;

	public CityWorldSettings getDefaults() {
		return defaults;
	}

	@Override
	public void onEnable() {
		saveDefaultConfig();

		loadConfig();
		loadComments();

		if(getConfig().getBoolean("enable-metrics", true)) {
			new SpigotMetrics(this, 13692);
		}

		addCommand("cityworld", new CommandCityWorld(this));
		addCommand("citychunk", new CommandCityChunk(this));
		addCommand("cityinfo", new CommandCityInfo(this)); // added by Sablednah (see below)

		// configFile can be retrieved via getConfig()
		defaults = CityWorldSettings.loadSettings(this);
		reportMessage("Enabled");

		getServer().getPluginManager().registerEvents(this, this);
	}

	public void reload() {
		saveDefaultConfig();

		loadConfig();
		loadComments();

		defaults = CityWorldSettings.loadSettings(this);

		for(CityWorldGenerator generator : this.generatorList.values()) {
			World world = Bukkit.getWorld(generator.worldName);

			if(world != null) {
				generator.initializeWorldInfo(world);
			}
		}
		loadConfig();
	}

	public void loadConfig() {
		FileConfiguration config = getConfig();
		config.addDefault("enable-metrics", true);

		saveConfig();
	}

	public void loadComments() {
		List<String> comments = Lists.newArrayList();
		comments.add("");
		comments.add(" ========== BSTATS METRICS ========================================================================");
		comments.add(" By default, the plugin collects and transmits anonymous statistics to bstats.org.");
		comments.add(" Data collection may be disabled here, or generally in the bStats/config.yml.");
		comments.add("");

		FileConfiguration config = getConfig();
		config.setComments("enable-metrics", comments);

		saveConfig();
	}

	private void addCommand(String keyword, CommandExecutor exec) {
		PluginCommand cmd = getCommand(keyword);
		if (cmd == null || exec == null) {
			reportMessage("[Lexicon] Cannot create command for " + keyword);
		} else {
			cmd.setExecutor(exec);
		}
	}

	@EventHandler
	public void onWorldLoad(WorldLoadEvent event) {
		// Extract our loot datapack to the default world folder, they're only loaded from here
		File datapack = new File(Bukkit.getWorlds().get(0).getWorldFolder(), "datapacks/cityworld");
//		reportMessage("DATAPACK = " + datapack.getAbsolutePath());
		boolean dataReload = extractResource("datapack/pack.mcmeta", new File(datapack, "pack.mcmeta"));

		// Exclude EMPTY and RANDOM
		for (LootProvider.LootLocation location : Arrays.copyOfRange(LootProvider.LootLocation.values(), 2, LootProvider.LootLocation.values().length)) {
			String path = "data/cityworld/loot_tables/chests/%s" + location.name().toLowerCase(Locale.ROOT) + ".json";
			// Destination should have world prefix, internal path doesn't
			// This allows for world specific loot tables
			File destination = new File(datapack, String.format(path, event.getWorld().getName().toLowerCase(Locale.ROOT) + "_"));
//			reportMessage("DESTINATION = " + destination.getAbsolutePath());
			dataReload = extractResource("datapack/" + String.format(path, ""), destination);
		}

		if (dataReload) {
			// Reload all server datapacks to ensure that we can actually use them at gen time
//			reportMessage("DATARELOAD!");
			Bukkit.reloadData();
		}
	}

	// I am curious, WHY?
//	@SuppressWarnings("ResultOfMethodCallIgnored")
	private boolean extractResource(String resource, File destination) {
		if (!destination.exists()) {
			if (!destination.getParentFile().exists() && !destination.getParentFile().mkdirs()) {
				reportMessage(">>> FAILED TO CREATE '" + destination.getParentFile().getPath() + "'");
			} else {
//				reportMessage("@@@ FOLDER EXISTS for '" + destination.getParentFile().getPath() +"'");
				try (InputStream stream = getResource(resource);
					 ReadableByteChannel rbc = Channels.newChannel(stream);
					 FileOutputStream fos = new FileOutputStream(destination)) {
//					reportMessage("### Transfering " + destination.getAbsolutePath());
					fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);

				} catch (Exception e) {
					reportFormatted("Unable to extract file %s (%s)", resource, e.getMessage());
				}
			}
			return true;
		}
		return false;
	}

	public String getPluginName() {
		return getDescription().getName();
	}

	private String getQuotedPluginName() {
		return "[" + getPluginName() + "]";
	}

	public void reportMessage(String message) {
		if (!message.startsWith("["))
			message = " " + message;
		log.info(getQuotedPluginName() + message);
	}

	public void reportMessage(String message1, String message2) {
		reportMessage(message1);
		log.info(" \\__" + message2);
	}

	public void reportFormatted(String format, Object... objects) {
		reportMessage(String.format(format, objects));
	}

	public void reportException(String message, Exception e) {
		reportMessage(message, "Exception: " + e.getMessage());
		e.printStackTrace();
	}

	// Added by Sablednah
	// https://github.com/echurchill/CityWorld/pull/4
	// Modified a bit by DaddyChurchill
	public CityWorldAPI getAPI(Plugin p) {
		if (p instanceof CityWorld)
			return new CityWorldAPI((CityWorld) p);
		else
			return null;
	}

}
