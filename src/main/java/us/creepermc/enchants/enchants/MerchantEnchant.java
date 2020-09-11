package us.creepermc.enchants.enchants;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import us.creepermc.enchants.Core;
import us.creepermc.enchants.managers.HookManager;
import us.creepermc.enchants.objects.ValueEnchant;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MerchantEnchant extends ValueEnchant {
	HookManager manager;
	
	public MerchantEnchant(YamlConfiguration config, Core core) {
		super(config, "merchant");
		manager = core.getManager(HookManager.class);
	}
	
	@Override
	public void apply(Player player, BlockBreakEvent event, int level) {
		manager.depositPlayer(player, getModifier(level));
	}
}