package us.creepermc.enchants.objects;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.bukkit.configuration.file.YamlConfiguration;

import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public abstract class ChanceEnchant extends Enchant {
	List<Double> chances;
	
	public ChanceEnchant(YamlConfiguration config, String id) {
		super(config, id);
		chances = new ArrayList<>();
		String chanceEquation = getSection().getString("chance-equation");
		for(int i = getMinLvl(); i <= getMaxLvl(); i++) chances.add(getChance(chanceEquation, i));
	}
	
	private double getChance(String chanceEquation, int level) {
		try {
			return Double.parseDouble(String.valueOf(getEngine().eval(chanceEquation.replace("level", String.valueOf(level))))) / 100;
		} catch(NumberFormatException | ScriptException ex) {
			return 0;
		}
	}
	
	public boolean pass(int level) {
		return chances.get(level - getMinLvl()) >= ThreadLocalRandom.current().nextDouble();
	}
}