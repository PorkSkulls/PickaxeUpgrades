package us.creepermc.enchants.managers;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import us.creepermc.enchants.Core;
import us.creepermc.enchants.templates.XManager;
import us.creepermc.mines.utils.Files;

import java.io.FileReader;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class StorageManager extends XManager {
	final Map<UUID, Long> energy = new HashMap<>();
	double expChance;
	int expAmount;
	int saveTask;
	
	public StorageManager(Core core) {
		super(core);
	}
	
	@Override
	public void initialize() {
		if(saveTask != 0) getCore().getServer().getScheduler().cancelTask(saveTask);
		if(!energy.isEmpty()) saveJSON("storage.json");
		energy.clear();
		
		expChance = getCore().getConfig().getDouble("pickaxe-exp.chance") / 100;
		expAmount = getCore().getConfig().getInt("pickaxe-exp.amount");
		loadJSON("storage.json");
		saveTask = new BukkitRunnable() {
			@Override
			public void run() {
				saveJSON("storage.json");
			}
		}.runTaskTimerAsynchronously(getCore(), 36000, 36000 /* 30 minutes */).getTaskId();
	}
	
	@Override
	public void deinitialize() {
		if(saveTask != 0) getCore().getServer().getScheduler().cancelTask(saveTask);
		saveJSON("storage.json");
		energy.clear();
		expChance = 0;
		expAmount = 0;
	}
	
	private void loadJSON(String file) {
		try {
			JSONObject json = (JSONObject) new JSONParser().parse(new FileReader(Files.getFile(getCore(), file, true)));
			((HashMap<String, Long>) json).forEach((k, v) -> energy.put(UUID.fromString(k), v));
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void saveJSON(String file) {
		JSONObject all = new JSONObject();
		energy.forEach((uuid, amount) -> all.put(uuid.toString(), amount));
		try {
			java.nio.file.Files.write(Paths.get(getCore().getDataFolder().getPath() + "/" + file), all.toJSONString().getBytes());
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void blockBreak(UUID uuid) {
		if(expChance < ThreadLocalRandom.current().nextDouble()) return;
		addExp(uuid, expAmount);
	}
	
	public long getExp(UUID uuid) {
		return energy.getOrDefault(uuid, 0L);
	}
	
	public void addExp(UUID uuid, int exp) {
		energy.put(uuid, getExp(uuid) + exp);
	}
	
	public void removeExp(UUID uuid, int exp) {
		energy.put(uuid, Math.max(0, getExp(uuid) - exp));
	}
	
	public void setExp(UUID uuid, int exp) {
		energy.put(uuid, (long) exp);
	}
}