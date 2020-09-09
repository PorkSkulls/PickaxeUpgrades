package us.creepermc.enchants.managers;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import us.creepermc.enchants.Core;
import us.creepermc.enchants.objects.Enchant;
import us.creepermc.enchants.templates.XInvManager;
import us.creepermc.enchants.utils.Files;
import us.creepermc.enchants.utils.Util;

import java.text.NumberFormat;
import java.util.Map;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class EnchantsInvManager extends XInvManager {
	EnchantManager enchantManager;
	PurchaseInvManager purchaseInvManager;
	StorageManager storageManager;
	
	public EnchantsInvManager(Core core) {
		super(core, "enchant_inventory");
	}
	
	@Override
	public void initialize() {
		deinitialize();
		super.initialize();
		
		enchantManager = getCore().getManager(EnchantManager.class);
		purchaseInvManager = getCore().getManager(PurchaseInvManager.class);
		storageManager = getCore().getManager(StorageManager.class);
	}
	
	@Override
	public void deinitialize() {
		super.deinitialize();
		enchantManager = null;
		purchaseInvManager = null;
		storageManager = null;
	}
	
	public void openInventory(Player player) {
		getCore().sendMsg(player, "OPEN_INVENTORY");
		Inventory copy = getCore().getServer().createInventory(null, getInventory().getInventory().getSize(), getInventory().getTitle());
		copy.setContents(getInventory().getInventory().getContents());
		long exp = storageManager.getExp(player.getUniqueId());
		getMenuItems().forEach(mitem -> {
			Enchant enchant = enchantManager.getEnchant(mitem.getId());
			if(enchant == null) {
				ItemStack item = mitem.getItem().clone();
				Util.setOwner(item, player.getName());
				item = Util.replace(item, new Files.Pair<>("{exp}", NumberFormat.getNumberInstance().format(exp)), new Files.Pair<>("{player}", player.getName()));
				copy.setItem(mitem.getSlot(), item);
				return;
			}
			copy.setItem(mitem.getSlot(), Util.replace(mitem.getItem().clone(), enchant.getPlaceholders(enchantManager.getEnchants(player.getItemInHand()))));
		});
		player.openInventory(copy);
	}
	
	@Override
	public void menuItemClick(InventoryClickEvent event, MenuItem menuItem) {
		Enchant enchant = enchantManager.getEnchant(menuItem.getId());
		if(enchant == null) return;
		Player player = (Player) event.getWhoClicked();
		Map<Enchant, Integer> enchants = enchantManager.getEnchants(player.getItemInHand());
		if(enchants.containsKey(enchant) && enchants.get(enchant) == enchant.getMaxLvl()) {
			getCore().sendMsg(player, "MAX_LEVEL");
			return;
		}
		purchaseInvManager.openInventory(player, enchant);
		player.setMetadata("pickaxeupgrades_enchant", new FixedMetadataValue(getCore(), enchant.getId()));
	}
	
	@Override
	public void invClose(Player player) {
		player.removeMetadata("pickaxeupgrades_enchant", getCore());
	}
	
	@EventHandler
	public void quit(PlayerQuitEvent event) {
		event.getPlayer().removeMetadata("pickaxeupgrades_enchant", getCore());
	}
	
	@EventHandler
	public void kick(PlayerKickEvent event) {
		event.getPlayer().removeMetadata("pickaxeupgrades_enchant", getCore());
	}
}
