package us.creepermc.enchants.objects;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import us.creepermc.enchants.utils.Util;

import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.List;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public abstract class EffectEnchant extends Enchant {
	PotionEffectType type;
	List<Integer> modifiers;
	
	public EffectEnchant(YamlConfiguration config, String id) {
		super(config, id);
		type = Util.getPotionEffect(getSection().getString("effect"));
		modifiers = new ArrayList<>();
		String modifierEquation = getSection().getString("modifier-equation");
		for(int i = getMinLvl(); i <= getMaxLvl(); i++) modifiers.add(getModifier(modifierEquation, i));
	}
	
	public void apply(Player player, int level) {
		player.addPotionEffect(new PotionEffect(getType(), Integer.MAX_VALUE, getModifier(level) - 1));
	}
	
	public void remove(Player player) {
		player.removePotionEffect(getType());
	}
	
	private int getModifier(String modifierEquation, int level) {
		try {
			return (int) Double.parseDouble(String.valueOf(getEngine().eval(modifierEquation.replace("level", String.valueOf(level)))));
		} catch(NumberFormatException | ScriptException ex) {
			return 0;
		}
	}
	
	private int getModifier(int level) {
		return modifiers.get(level - getMinLvl());
	}
}