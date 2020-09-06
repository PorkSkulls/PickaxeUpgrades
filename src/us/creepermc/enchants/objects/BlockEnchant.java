package us.creepermc.enchants.objects;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public abstract class BlockEnchant extends ChanceEnchant {
	
	public BlockEnchant(YamlConfiguration config, String id) {
		super(config, id);
	}
	
	public abstract void apply(Player player, BlockBreakEvent event, int level);
}
