package us.creepermc.enchants.managers;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.brcdev.shopgui.ShopGuiPlusApi;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import us.creepermc.enchants.Core;
import us.creepermc.enchants.templates.XManager;
import us.creepermc.enchants.utils.Files;
import us.creepermc.enchants.utils.Util;
import us.creepermc.mines.managers.StorageManager;
import us.creepermc.mines.objects.PlayerMine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class HookManager extends XManager {
	final List<String> regions = new ArrayList<>();
	final Economy economy;
	final WorldGuardPlugin worldGuard;
	StorageManager islandMinesManager;
	
	public HookManager(Core core) {
		super(core);
		economy = Util.setupVault(Economy.class);
		worldGuard = (WorldGuardPlugin) getCore().getServer().getPluginManager().getPlugin("WorldGuard");
	}
	
	@Override
	public void initialize() {
		deinitialize();
		
		regions.addAll(getCore().getConfig().getStringList("valid-regions"));
		checkStorageManager();
	}
	
	@Override
	public void deinitialize() {
		regions.clear();
	}
	
	public void depositPlayer(Player player, double amount) {
		economy.depositPlayer(player, amount);
	}
	
	public void sell(Player player, Collection<ItemStack> items) {
		try {
			double total = 0;
			for(ItemStack item : items) total += ShopGuiPlusApi.getItemStackPriceSell(player, item);
			economy.depositPlayer(player, total);
		} catch(Exception ex) {
		}
	}
	
	public boolean isValidLocation(Location location) {
		return isValidRegion(location) || isMine(location);
	}
	
	public Files.Pair<PlayerMine, List<Location>> get3x3(Location location) {
		List<Location> locations = new ArrayList<>();
		if(!isValidLocation(location)) return new Files.Pair<>(null, locations);
		ProtectedRegion region = getValidRegion(location);
		PlayerMine mine = getMine(location);
		for(int x = -1; x <= 1; x++)
			for(int y = -1; y <= 1; y++)
				for(int z = -1; z <= 1; z++) {
					Location checking = location.clone().add(x, y, z);
					if(region != null && region.contains(checking.getBlockX(), checking.getBlockY(), checking.getBlockZ()) || mine != null && mine.isInMine(checking)) {
						if(checking.getBlock().getType() == Material.BEDROCK || checking.getBlock().getType() == Material.AIR) continue;
						locations.add(checking);
					}
				}
		return new Files.Pair<>(mine, locations);
	}
	
	public Files.Pair<PlayerMine, List<Location>> getLayer(Location location) {
		List<Location> locations = new ArrayList<>();
		if(!isValidLocation(location)) return new Files.Pair<>(null, locations);
		ProtectedRegion region = getValidRegion(location);
		if(region != null) {
			for(int x = region.getMinimumPoint().getBlockX(); x <= region.getMaximumPoint().getBlockX(); x++)
				for(int z = region.getMinimumPoint().getBlockZ(); z <= region.getMaximumPoint().getBlockZ(); z++) {
					Location checking = new Location(location.getWorld(), x, location.getY(), z);
					if(checking.getBlock().getType() == Material.BEDROCK || checking.getBlock().getType() == Material.AIR) continue;
					locations.add(checking);
				}
			return new Files.Pair<>(null, locations);
		}
		PlayerMine mine = getMine(location);
		for(int x = mine.getPlaced().getBlockX(); x <= mine.getPlaced().getBlockX() + mine.getMine().getSize() + 1; x++)
			for(int z = mine.getPlaced().getBlockZ(); z <= mine.getPlaced().getBlockZ() + mine.getMine().getSize() + 1; z++) {
				Location checking = new Location(location.getWorld(), x, location.getY(), z);
				if(checking.getBlock().getType() == Material.BEDROCK || checking.getBlock().getType() == Material.AIR) continue;
				locations.add(checking);
			}
		return new Files.Pair<>(mine, locations);
	}
	
	public Files.Pair<PlayerMine, List<Location>> getEntireMine(Location location) {
		List<Location> locations = new ArrayList<>();
		if(!isValidLocation(location)) return new Files.Pair<>(null, locations);
		ProtectedRegion region = getValidRegion(location);
		if(region != null) {
			for(int y = region.getMinimumPoint().getBlockY(); y <= region.getMaximumPoint().getBlockY(); y++)
				for(int x = region.getMinimumPoint().getBlockX(); x <= region.getMaximumPoint().getBlockX(); x++)
					for(int z = region.getMinimumPoint().getBlockZ(); z <= region.getMaximumPoint().getBlockZ(); z++) {
						Location checking = new Location(location.getWorld(), x, y, z);
						if(checking.getBlock().getType() == Material.BEDROCK || checking.getBlock().getType() == Material.AIR) continue;
						locations.add(checking);
					}
			return new Files.Pair<>(null, locations);
		}
		PlayerMine mine = getMine(location);
		for(int y = mine.getPlaced().getBlockY(); y <= mine.getPlaced().getBlockY() + mine.getMine().getHeight() + 1; y++)
			for(int x = mine.getPlaced().getBlockX(); x <= mine.getPlaced().getBlockX() + mine.getMine().getSize() + 1; x++)
				for(int z = mine.getPlaced().getBlockZ(); z <= mine.getPlaced().getBlockZ() + mine.getMine().getSize() + 1; z++) {
					Location checking = new Location(location.getWorld(), x, y, z);
					if(checking.getBlock().getType() == Material.BEDROCK || checking.getBlock().getType() == Material.AIR) continue;
					locations.add(checking);
				}
		return new Files.Pair<>(mine, locations);
	}
	
	private ProtectedRegion getValidRegion(Location location) {
		for(ProtectedRegion region : worldGuard.getRegionManager(location.getWorld()).getApplicableRegions(location).getRegions())
			if(region != null && regions.contains(region.getId().toLowerCase()))
				return region;
		return null;
	}
	
	private boolean isValidRegion(Location location) {
		return getValidRegion(location) != null;
	}
	
	public PlayerMine getMine(Location location) {
		checkStorageManager();
		return islandMinesManager.getMine(location);
	}
	
	public boolean isMine(Location location) {
		return getMine(location) != null;
	}
	
	private void checkStorageManager() {
		if(islandMinesManager != null) return;
		islandMinesManager = ((us.creepermc.mines.Core) getCore().getServer().getPluginManager().getPlugin("IslandMines")).getManager(StorageManager.class);
	}
}