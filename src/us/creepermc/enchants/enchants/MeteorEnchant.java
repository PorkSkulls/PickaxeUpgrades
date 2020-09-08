package us.creepermc.enchants.enchants;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import us.creepermc.enchants.Core;
import us.creepermc.enchants.managers.HookManager;
import us.creepermc.enchants.objects.BlockEnchant;
import us.creepermc.enchants.utils.BlockUtil;
import us.creepermc.enchants.utils.Files;
import us.creepermc.mines.objects.PlayerMine;

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
		Files.Pair<PlayerMine, List<Location>> pair = manager.getEntireMine(event.getBlock().getLocation());
		List<Location> locations = pair.getValue();
		if(locations.size() <= 1) return;
		ItemStack pickaxe = player.getItemInHand();
		List<ItemStack> drops = new ArrayList<>();
		locations.forEach(loc -> {
			if(pair.getKey() == null) drops.addAll(loc.getBlock().getDrops(pickaxe));
			else pair.getKey().addProgress();
			BlockUtil.setBlockInNativeChunkSection(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), 0, (byte) 0);
		});
		if(pair.getKey() != null) {
			PlayerMine mine = pair.getKey();
			Block block = locations.get(0).getBlock();
			boolean redstoneMatch = block.getType() == Material.GLOWING_REDSTONE_ORE && mine.getUpgrade().getData().getItemType() == Material.REDSTONE_ORE;
			if(!block.getState().getData().equals(mine.getUpgrade().getData()) && !redstoneMatch) return;
			mine.addStorage(redstoneMatch ? mine.getUpgrade().getData() : block.getState().getData(), locations.size());
		}
		if(drops.isEmpty()) return;
		simplifyItems(drops);
		drops.forEach(item -> {
			if(player.getInventory().firstEmpty() != -1) player.getInventory().addItem(item);
			else player.getWorld().dropItemNaturally(player.getLocation(), item);
		});
	}
}