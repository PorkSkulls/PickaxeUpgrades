package us.creepermc.enchants.enchants;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import us.creepermc.enchants.Core;
import us.creepermc.enchants.managers.HookManager;
import us.creepermc.enchants.objects.BlockEnchant;
import us.creepermc.enchants.utils.Files;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MeteorEnchant extends BlockEnchant {
	Map<String, Integer> tasks = new HashMap<>();
	Core core;
	HookManager manager;
	int interval;
	double percent;
	
	public MeteorEnchant(YamlConfiguration config, Core core) {
		super(config, "meteor");
		this.core = core;
		manager = core.getManager(HookManager.class);
		interval = getSection().getInt("interval");
		percent = getSection().getDouble("percent") / 100.0;
	}
	
	@Override
	public void deinitialize() {
		tasks.values().forEach(core.getServer().getScheduler()::cancelTask);
		tasks.clear();
	}
	
	@Override
	public void apply(Player player, BlockBreakEvent event, int level) {
		Files.Pair<String, List<Location>> pair = manager.getEntireMine(event.getBlock().getLocation());
		List<Location> locations = pair.getValue();
		if(tasks.containsKey(pair.getKey()) || locations.isEmpty()) return;
		int amount = (int) (locations.size() * percent);
		ItemStack pickaxe = player.getItemInHand();
		tasks.put(pair.getKey(), new BukkitRunnable() {
			int index = 0;
			
			@Override
			public void run() {
				List<ItemStack> drops = new ArrayList<>();
				int min = index * amount;
				int max = Math.min(locations.size(), (index + 1) * amount);
				locations.subList(min, max).forEach(loc -> {
					drops.addAll(loc.getBlock().getDrops(pickaxe));
					loc.getBlock().setType(Material.AIR);
				});
				drops.forEach(item -> {
					if(player.getInventory().firstEmpty() != -1) player.getInventory().addItem(item);
					else player.getWorld().dropItemNaturally(player.getLocation(), item);
				});
				index++;
				if(max >= locations.size()) {
					tasks.remove(pair.getKey());
					cancel();
				}
			}
		}.runTaskTimer(core, 0, interval).getTaskId());
	}
}