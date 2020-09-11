package us.creepermc.enchants.enchants;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import us.creepermc.enchants.Core;
import us.creepermc.enchants.managers.HookManager;
import us.creepermc.enchants.objects.BlockEnchant;
import us.creepermc.mines.objects.PlayerMine;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class DoubleBlockEnchant extends BlockEnchant {
	final Core core;
	HookManager manager;
	
	public DoubleBlockEnchant(YamlConfiguration config, Core core) {
		super(config, "doubleblock");
		this.core = core;
	}
	
	@Override
	public void initialize() {
		manager = core.getManager(HookManager.class);
	}
	
	@Override
	public void deinitialize() {
		manager = null;
	}
	
	@Override
	public void apply(Player player, BlockBreakEvent event, int level) {
		Block block = event.getBlock();
		if(manager.isMine(block.getLocation())) {
			PlayerMine mine = manager.getMine(block.getLocation());
			mine.addStorage(block.getState().getData(), 1);
			return;
		}
		block.getDrops(player.getItemInHand()).forEach(item -> player.getWorld().dropItemNaturally(block.getLocation(), item));
	}
}