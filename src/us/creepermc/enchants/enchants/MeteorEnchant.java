package us.creepermc.enchants.enchants;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import us.creepermc.enchants.Core;
import us.creepermc.enchants.managers.HookManager;
import us.creepermc.enchants.objects.BlockEnchant;
import us.creepermc.enchants.utils.BlockUtil;

import java.util.ArrayList;
import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MeteorEnchant extends BlockEnchant {
	HookManager manager;
	
	public MeteorEnchant(YamlConfiguration config, Core core) {
		super(config, "meteor");
		manager = core.getManager(HookManager.class);
	}
	
	@Override
	public void apply(Player player, BlockBreakEvent event, int level) {
		List<Location> locations = manager.getEntireMine(event.getBlock().getLocation());
		if(locations.size() <= 1) return;
		ItemStack pickaxe = player.getItemInHand();
		List<ItemStack> drops = new ArrayList<>();
		locations.forEach(loc -> {
			drops.addAll(loc.getBlock().getDrops(pickaxe));
			BlockUtil.setBlockInNativeChunkSection(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), 0, (byte) 0);
		});
		simplifyItems(drops);
		drops.forEach(item -> {
			if(player.getInventory().firstEmpty() != -1) player.getInventory().addItem(item);
			else player.getWorld().dropItemNaturally(player.getLocation(), item);
		});
	}
}