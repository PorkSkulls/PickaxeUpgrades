package us.creepermc.enchants.objects;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import us.creepermc.enchants.utils.Util;

import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public abstract class VanillaEnchant extends Enchant {
	Enchantment enchant;
	List<Integer> modifiers;
	
	public VanillaEnchant(YamlConfiguration config, String id) {
		super(config, id);
		enchant = Util.getEnchantment(getSection().getString("enchant"));
		modifiers = new ArrayList<>();
		String modifierEquation = getSection().getString("modifier-equation");
		for(int i = getMinLvl(); i <= getMaxLvl(); i++) modifiers.add(getModifier(modifierEquation, i));
	}
	
	@Override
	public void apply(ItemStack item, Map<Enchant, Integer> enchants, int level) {
		super.apply(item, enchants, level);
		ItemMeta meta = item.getItemMeta();
		int hasLevel = meta.hasEnchant(enchant) ? meta.getEnchantLevel(enchant) : 0;
		meta.addEnchant(enchant, getModifier(hasLevel + level), true);
		if(!meta.hasItemFlag(ItemFlag.HIDE_ENCHANTS)) meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		item.setItemMeta(meta);
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
