package us.creepermc.enchants.objects;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import us.creepermc.enchants.utils.Files;
import us.creepermc.enchants.utils.RomanNumerals;
import us.creepermc.enchants.utils.Util;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public abstract class Enchant {
	String id;
	ConfigurationSection section;
	boolean enabled;
	String name;
	String description;
	List<Double> costs;
	int minLvl;
	int maxLvl;
	List<String> display;
	ScriptEngine engine;
	
	public Enchant(YamlConfiguration config, String id) {
		this.id = id;
		section = config.getConfigurationSection("enchants." + id);
		enabled = section.getBoolean("enabled");
		name = section.getString("name");
		description = section.getString("description");
		costs = new ArrayList<>();
		minLvl = 1;
		maxLvl = section.getInt("maxlvl");
		display = new ArrayList<>();
		String displayTemplate = Util.color(section.getString("display"));
		for(int i = minLvl; i <= maxLvl; i++)
			display.add(displayTemplate.replace("{name}", name).replace("{level}", String.valueOf(i)).replace("{level_roman}", RomanNumerals.convertToRoman(i)));
		engine = new ScriptEngineManager().getEngineByName("JavaScript");
		String costEquation = section.getString("cost-equation");
		for(int i = minLvl; i <= maxLvl; i++) costs.add(getCost(costEquation, i));
	}
	
	public void initialize() {
	}
	
	public void deinitialize() {
	}
	
	public void apply(ItemStack item, Map<Enchant, Integer> enchants, int level) {
		ItemMeta meta = item.getItemMeta();
		List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
		if(enchants.containsKey(this)) lore.removeAll(lore.stream().filter(this::isEnchant).collect(Collectors.toList()));
		lore.add(display.get(Math.min(maxLvl, enchants.getOrDefault(this, 0) + level) - minLvl));
		meta.setLore(lore);
		item.setItemMeta(meta);
	}
	
	public void remove(ItemStack item) {
		ItemMeta meta = item.getItemMeta();
		if(!meta.hasLore()) return;
		List<String> lore = meta.getLore();
		lore.removeAll(lore.stream().filter(this::isEnchant).collect(Collectors.toList()));
		meta.setLore(lore);
		item.setItemMeta(meta);
	}
	
	public boolean isEnchant(String line) {
		return display.contains(line);
	}
	
	public int getPrice(Map<Enchant, Integer> enchants, int levels) {
		int level = enchants.getOrDefault(this, 0);
		double totalPrice = 0;
		for(int i = level + 1; i <= level + levels; i++) {
			if(i > maxLvl) break;
			totalPrice += getCost(i);
		}
		return (int) totalPrice;
	}
	
	public List<Files.Pair<String, String>> getPlaceholders(Map<Enchant, Integer> enchants) {
		List<Files.Pair<String, String>> placeholders = new ArrayList<>(getPlaceholders());
		int level = enchants.getOrDefault(this, 0);
		placeholders.add(new Files.Pair<>("{level}", NumberFormat.getNumberInstance().format(level)));
		placeholders.add(new Files.Pair<>("{cost}", level == maxLvl ? "N/A" : NumberFormat.getNumberInstance().format(getCost(level + 1))));
		return placeholders;
	}
	
	private List<Files.Pair<String, String>> getPlaceholders() {
		return Arrays.asList(
				new Files.Pair<>("{name}", name),
				new Files.Pair<>("{description}", description),
				new Files.Pair<>("{minlevel}", NumberFormat.getNumberInstance().format(minLvl)),
				new Files.Pair<>("{maxlevel}", NumberFormat.getNumberInstance().format(maxLvl))
		);
	}
	
	private double getCost(String costEquation, int level) {
		try {
			return Double.parseDouble(String.valueOf(engine.eval(costEquation.replace("level", String.valueOf(level)))));
		} catch(NumberFormatException | ScriptException ex) {
			return 0;
		}
	}
	
	private double getCost(int level) {
		return costs.get(level - minLvl);
	}
}
