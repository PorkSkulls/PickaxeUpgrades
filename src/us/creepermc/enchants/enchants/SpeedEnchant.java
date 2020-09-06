package us.creepermc.enchants.enchants;

import org.bukkit.configuration.file.YamlConfiguration;
import us.creepermc.enchants.objects.EffectEnchant;

public class SpeedEnchant extends EffectEnchant {
	public SpeedEnchant(YamlConfiguration config) {
		super(config, "speed");
	}
}