package us.creepermc.enchants.enchants;

import org.bukkit.configuration.file.YamlConfiguration;
import us.creepermc.enchants.objects.VanillaEnchant;

public class UnbreakingEnchant extends VanillaEnchant {
	public UnbreakingEnchant(YamlConfiguration config) {
		super(config, "unbreaking");
	}
}