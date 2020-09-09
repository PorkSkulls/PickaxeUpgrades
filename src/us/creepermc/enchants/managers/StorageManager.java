package us.creepermc.enchants.managers;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import us.creepermc.enchants.Core;
import us.creepermc.enchants.templates.XManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class StorageManager extends XManager {
	final Map<UUID, Long> energy = new HashMap<>();
	double expChance;
	int expAmount;
	
	public StorageManager(Core core) {
		super(core);
	}
	
	@Override
	public void initialize() {
		deinitialize();
		
		expChance = getCore().getConfig().getDouble("pickaxe-exp.chance") / 100;
		expAmount = getCore().getConfig().getInt("pickaxe-exp.amount");
	}
	
	@Override
	public void deinitialize() {
		expChance = 0;
		expAmount = 0;
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