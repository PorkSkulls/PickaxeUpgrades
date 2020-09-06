package us.creepermc.enchants.managers;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import us.creepermc.enchants.Core;
import us.creepermc.enchants.objects.ItemPick;
import us.creepermc.enchants.templates.XManager;
import us.creepermc.enchants.utils.NBTEditor;

import java.util.concurrent.ThreadLocalRandom;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class PickaxeManager extends XManager {
	EnchantManager enchantManager;
	double expChance;
	int expAmount;
	
	public PickaxeManager(Core core) {
		super(core);
	}
	
	@Override
	public void initialize() {
		deinitialize();
		
		enchantManager = getCore().getManager(EnchantManager.class);
		expChance = getCore().getConfig().getDouble("pickaxe-exp.chance") / 100;
		expAmount = getCore().getConfig().getInt("pickaxe-exp.amount");
	}
	
	@Override
	public void deinitialize() {
		enchantManager = null;
		expChance = 0;
		expAmount = 0;
	}
	
	public void initializePickaxe(Player player) {
		ItemStack item = player.getItemInHand();
		if(!NBTEditor.get(item, "pickaxeupgrades_exp").isEmpty()) return;
		item = setExp(item, 0);
		player.setItemInHand(item);
	}
	
	public ItemPick getItemPick(ItemStack item) {
		return new ItemPick(getExp(item), enchantManager.getEnchants(item));
	}
	
	public int getExp(ItemStack item) {
		String get = NBTEditor.get(item, "pickaxeupgrades_exp");
		return get.isEmpty() ? 0 : Integer.parseInt(get);
	}
	
	public ItemStack blockBreak(ItemStack item) {
		if(expChance < ThreadLocalRandom.current().nextDouble()) return item;
		return addExp(item, expAmount);
	}
	
	public ItemStack addExp(ItemStack item, int exp) {
		return NBTEditor.set(item, "pickaxeupgrades_exp", String.valueOf(getExp(item) + exp));
	}
	
	public ItemStack removeExp(ItemStack item, int exp) {
		return NBTEditor.set(item, "pickaxeupgrades_exp", String.valueOf(Math.max(getExp(item) - exp, 0)));
	}
	
	public ItemStack setExp(ItemStack item, int exp) {
		return NBTEditor.set(item, "pickaxeupgrades_exp", String.valueOf(exp));
	}
}
