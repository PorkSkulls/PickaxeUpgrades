package us.creepermc.enchants.enchants;

import org.bukkit.configuration.file.YamlConfiguration;
import us.creepermc.enchants.objects.EffectEnchant;

public class JumpBoostEnchant extends EffectEnchant {
	public JumpBoostEnchant(YamlConfiguration config) {
		super(config, "jumpboost");
	}
}