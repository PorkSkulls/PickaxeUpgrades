package us.creepermc.enchants.objects;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.bukkit.configuration.file.YamlConfiguration;

import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.List;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public abstract class ValueEnchant extends BlockEnchant {
	List<Double> modifiers;
	
	public ValueEnchant(YamlConfiguration config, String id) {
		super(config, id);
		modifiers = new ArrayList<>();
		String modifierEquation = getSection().getString("modifier-equation");
		for(int i = getMinLvl(); i <= getMaxLvl(); i++) modifiers.add(getModifier(modifierEquation, i));
	}
	
	private double getModifier(String modifierEquation, int level) {
		try {
			return Double.parseDouble(String.valueOf(getEngine().eval(modifierEquation.replace("level", String.valueOf(level)))));
		} catch(NumberFormatException | ScriptException ex) {
			return 0;
		}
	}
	
	public double getModifier(int level) {
		return modifiers.get(level - getMinLvl());
	}
}