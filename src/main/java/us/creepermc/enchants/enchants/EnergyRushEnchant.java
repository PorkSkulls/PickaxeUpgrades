package us.creepermc.enchants.enchants;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import us.creepermc.enchants.Core;
import us.creepermc.enchants.managers.StorageManager;
import us.creepermc.enchants.objects.ValueEnchant;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class EnergyRushEnchant extends ValueEnchant {
	final Map<UUID, Integer> tasks = new HashMap<>();
	final Core core;
	StorageManager manager;
	
	public EnergyRushEnchant(YamlConfiguration config, Core core) {
		super(config, "energyrush");
		this.core = core;
	}
	
	@Override
	public void initialize() {
		deinitialize();
		
		manager = core.getManager(StorageManager.class);
	}
	
	@Override
	public void deinitialize() {
		tasks.values().forEach(core.getServer().getScheduler()::cancelTask);
		tasks.clear();
		manager = null;
	}
	
	@Override
	public void apply(Player player, BlockBreakEvent event, int level) {
		if(tasks.containsKey(player.getUniqueId())) core.getServer().getScheduler().cancelTask(tasks.remove(player.getUniqueId()));
		else core.sendMsg(player, "ENERGYRUSH_ACTIVATED");
		tasks.put(player.getUniqueId(), new BukkitRunnable() {
			@Override
			public void run() {
				core.sendMsg(player, "ENERGYRUSH_DEACTIVATED");
				tasks.remove(player.getUniqueId());
			}
		}.runTaskLaterAsynchronously(core, (int) getModifier(level)).getTaskId());
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void exprush(BlockBreakEvent event) {
		Player player = event.getPlayer();
		if(!tasks.containsKey(player.getUniqueId())) return;
		manager.blockBreak(player.getUniqueId());
	}
	
	@EventHandler
	public void quit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		if(!tasks.containsKey(player.getUniqueId())) return;
		core.getServer().getScheduler().cancelTask(tasks.remove(player.getUniqueId()));
	}
	
	@EventHandler
	public void kick(PlayerKickEvent event) {
		Player player = event.getPlayer();
		if(!tasks.containsKey(player.getUniqueId())) return;
		core.getServer().getScheduler().cancelTask(tasks.remove(player.getUniqueId()));
	}
}