package us.creepermc.enchants.enchants;

import org.bukkit.configuration.file.YamlConfiguration;
import us.creepermc.enchants.objects.EffectEnchant;

public class HasteEnchant extends EffectEnchant {
	public HasteEnchant(YamlConfiguration config) {
		super(config, "haste");
	}
}