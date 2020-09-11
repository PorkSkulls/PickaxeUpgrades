package us.creepermc.enchants.enchants;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import us.creepermc.enchants.Core;
import us.creepermc.enchants.managers.HookManager;
import us.creepermc.enchants.managers.StorageManager;
import us.creepermc.enchants.objects.BlockEnchant;
import us.creepermc.enchants.utils.BlockUtil;
import us.creepermc.enchants.utils.Files;
import us.creepermc.mines.objects.PlayerMine;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JackhammerEnchant extends BlockEnchant {
	HookManager hookManager;
	StorageManager storageManager;
	
	public JackhammerEnchant(YamlConfiguration config, Core core) {
		super(config, "jackhammer");
		hookManager = core.getManager(HookManager.class);
		storageManager = core.getManager(StorageManager.class);
	}
	
	@Override
	public void apply(Player player, BlockBreakEvent event, int level) {
		CompletableFuture.runAsync(() -> {
			Files.Pair<PlayerMine, List<Location>> pair = hookManager.getLayer(event.getBlock().getLocation());
			List<Location> locations = pair.getValue();
			if(locations.size() <= 1) return;
			ItemStack pickaxe = player.getItemInHand();
			List<ItemStack> drops = new ArrayList<>();
			MaterialData data = locations.get(0).getBlock().getState().getData();
			locations.forEach(loc -> {
				if(pair.getKey() == null) drops.addAll(loc.getBlock().getDrops(pickaxe));
				else pair.getKey().addProgress();
				storageManager.blockBreak(player.getUniqueId());
				BlockUtil.setBlockInNativeChunkSection(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), 0, (byte) 0);
			});
			if(pair.getKey() != null) {
				PlayerMine mine = pair.getKey();
				boolean redstoneMatch = data.getItemType() == Material.GLOWING_REDSTONE_ORE && mine.getUpgrade().getData().getItemType() == Material.REDSTONE_ORE;
				if(!data.equals(mine.getUpgrade().getData()) && !redstoneMatch) return;
				mine.addStorage(redstoneMatch ? mine.getUpgrade().getData() : data, locations.size());
			}
			if(drops.isEmpty()) return;
			simplifyItems(drops);
			for(ItemStack item : drops) {
				if(player.getInventory().firstEmpty() == -1) {
					hookManager.getCore().sendMsg(player, "FULL_INVENTORY");
					break;
				}
				player.getInventory().addItem(item);
			}
		});
	}
}