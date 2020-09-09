package us.creepermc.enchants.managers;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import us.creepermc.enchants.Core;
import us.creepermc.enchants.enchants.*;
import us.creepermc.enchants.objects.Enchant;
import us.creepermc.enchants.templates.XManager;
import us.creepermc.enchants.utils.Files;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EnchantManager extends XManager {
	List<Enchant> enchants = new ArrayList<>();
	
	public EnchantManager(Core core) {
		super(core);
	}
	
	@Override
	public void initialize() {
		deinitialize();
		
		YamlConfiguration config = Files.getConfiguration(Files.getFile(getCore(), "enchants.yml", true));
		new BukkitRunnable() {
			@Override
			public void run() {
				if(config.isConfigurationSection("enchants")) {
					if(config.isSet("enchants.jackhammer")) enchants.add(new JackhammerEnchant(config, getCore()));
					if(config.isSet("enchants.explosive")) enchants.add(new ExplosiveEnchant(config, getCore()));
					if(config.isSet("enchants.meteor")) enchants.add(new MeteorEnchant(config, getCore()));
					if(config.isSet("enchants.autosell")) enchants.add(new AutosellEnchant(config, getCore()));
					if(config.isSet("enchants.doubleblock")) enchants.add(new DoubleBlockEnchant(config, getCore()));
					if(config.isSet("enchants.energyrush")) enchants.add(new EnergyRushEnchant(config, getCore()));
					if(config.isSet("enchants.merchant")) enchants.add(new MerchantEnchant(config, getCore()));
					if(config.isSet("enchants.efficiency")) enchants.add(new EfficiencyEnchant(config));
					if(config.isSet("enchants.fortune")) enchants.add(new FortuneEnchant(config));
					if(config.isSet("enchants.unbreaking")) enchants.add(new UnbreakingEnchant(config));
					if(config.isSet("enchants.speed")) enchants.add(new SpeedEnchant(config));
					if(config.isSet("enchants.haste")) enchants.add(new HasteEnchant(config));
					if(config.isSet("enchants.jumpboost")) enchants.add(new JumpBoostEnchant(config));
				}
				enchants.forEach(enchant -> {
					if(enchant.getClass().isAssignableFrom(Listener.class))
						getCore().getServer().getPluginManager().registerEvents((Listener) enchant, getCore());
					enchant.initialize();
				});
			}
		}.runTaskAsynchronously(getCore());
	}
	
	@Override
	public void deinitialize() {
		enchants.forEach(Enchant::deinitialize);
		enchants.clear();
	}
	
	public Enchant getEnchant(String id) {
		return enchants.stream().filter(enchant -> enchant.getId().equalsIgnoreCase(id) || enchant.getName().equalsIgnoreCase(id)).findFirst().orElse(null);
	}
	
	public Map<Enchant, Integer> getEnchants(ItemStack item) {
		Map<Enchant, Integer> enchants = new HashMap<>();
		if(item == null || item.getType() == Material.AIR || !item.hasItemMeta() || !item.getItemMeta().hasLore()) return enchants;
		for(String line : item.getItemMeta().getLore()) {
			Files.Pair<Enchant, Integer> enchant = getEnchantFromDisplay(line);
			if(enchant == null) continue;
			enchants.put(enchant.getKey(), enchant.getValue());
		}
		return enchants;
	}
	
	public Files.Pair<Enchant, Integer> getEnchantFromDisplay(String line) {
		return enchants.stream().filter(enchant -> enchant.isEnchant(line)).map(enchant -> {
			for(int i = enchant.getMinLvl(); i <= enchant.getMaxLvl(); i++)
				if(enchant.getDisplay().get(i - enchant.getMinLvl()).equalsIgnoreCase(line))
					return new Files.Pair<>(enchant, i);
			return null;
		}).findFirst().orElse(null);
	}
}
