package us.creepermc.enchants.enchants;

import org.bukkit.configuration.file.YamlConfiguration;
import us.creepermc.enchants.objects.VanillaEnchant;

public class EfficiencyEnchant extends VanillaEnchant {
	public EfficiencyEnchant(YamlConfiguration config) {
		super(config, "efficiency");
	}
}