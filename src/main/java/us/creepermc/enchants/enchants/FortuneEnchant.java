package us.creepermc.enchants.enchants;

import org.bukkit.configuration.file.YamlConfiguration;
import us.creepermc.enchants.objects.VanillaEnchant;

public class FortuneEnchant extends VanillaEnchant {
	public FortuneEnchant(YamlConfiguration config) {
		super(config, "fortune");
	}
}