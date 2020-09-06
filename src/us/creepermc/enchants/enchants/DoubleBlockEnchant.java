package us.creepermc.enchants.enchants;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import us.creepermc.enchants.objects.BlockEnchant;

public class DoubleBlockEnchant extends BlockEnchant {
	
	public DoubleBlockEnchant(YamlConfiguration config) {
		super(config, "doubleblock");
	}
	
	@Override
	public void apply(Player player, BlockBreakEvent event, int level) {
		event.getBlock().getDrops(player.getItemInHand()).forEach(item -> player.getWorld().dropItemNaturally(event.getBlock().getLocation(), item));
	}
}