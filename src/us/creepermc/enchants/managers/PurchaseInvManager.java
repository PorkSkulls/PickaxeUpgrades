package us.creepermc.enchants.managers;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import us.creepermc.enchants.Core;
import us.creepermc.enchants.objects.Enchant;
import us.creepermc.enchants.templates.XInvManager;
import us.creepermc.enchants.utils.Files;
import us.creepermc.enchants.utils.Util;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class PurchaseInvManager extends XInvManager {
	EnchantsInvManager enchantsInvManager;
	EnchantManager enchantManager;
	StorageManager storageManager;
	
	public PurchaseInvManager(Core core) {
		super(core, "purchase_inventory");
	}
	
	@Override
	public void initialize() {
		deinitialize();
		super.initialize();
		
		enchantsInvManager = getCore().getManager(EnchantsInvManager.class);
		enchantManager = getCore().getManager(EnchantManager.class);
		storageManager = getCore().getManager(StorageManager.class);
	}
	
	@Override
	public void deinitialize() {
		super.deinitialize();
		enchantsInvManager = null;
		enchantManager = null;
		storageManager = null;
	}
	
	public void openInventory(Player player, Enchant enchant) {
		getCore().sendMsg(player, "OPEN_PURCHASE_INVENTORY");
		Inventory copy = getCore().getServer().createInventory(null, getInventory().getInventory().getSize(), getInventory().getTitle());
		copy.setContents(getInventory().getInventory().getContents());
		updateMenuItems(copy, player, enchant);
		player.openInventory(copy);
	}
	
	@Override
	public void menuItemClick(InventoryClickEvent event, MenuItem menuItem) {
		Player player = (Player) event.getWhoClicked();
		if(!(menuItem instanceof PurchaseItem)) return;
		PurchaseItem purchaseItem = (PurchaseItem) menuItem;
		Enchant enchant = getMetaEnchant(player);
		if(enchant == null) return;
		ItemStack pick = player.getItemInHand();
		Map<Enchant, Integer> enchants = enchantManager.getEnchants(pick);
		long exp = storageManager.getExp(player.getUniqueId());
		int levels = (int) purchaseItem.getPrice();
		int price = enchant.getPrice(enchants, levels);
		if(exp < price) {
			getCore().sendMsg(player, "NOT_ENOUGH_EXP", String.valueOf(price));
			return;
		}
		storageManager.removeExp(player.getUniqueId(), price);
		enchant.apply(pick, enchants, levels);
		getCore().sendMsg(player, "PURCHASED", new Files.Pair<>("{enchant}", enchant.getName()), new Files.Pair<>("{level}", String.valueOf(levels)));
		updateMenuItems(event.getInventory(), player, enchant);
	}
	
	@Override
	public void invClose(Player player) {
		new BukkitRunnable() {
			@Override
			public void run() {
				if(player == null || !player.isOnline()) return;
				enchantsInvManager.openInventory(player);
			}
		}.runTask(getCore());
	}
	
	private void updateMenuItems(Inventory inv, Player player, Enchant enchant) {
		long exp = storageManager.getExp(player.getUniqueId());
		Map<Enchant, Integer> enchants = enchantManager.getEnchants(player.getItemInHand());
		getMenuItems().forEach(mitem -> {
			if(mitem.getType() == MenuItemType.PURCHASE) {
				List<Files.Pair<String, String>> placeholders = new ArrayList<>();
				placeholders.add(new Files.Pair<>("{cost}", NumberFormat.getNumberInstance().format(enchant.getPrice(enchants, (int) ((PurchaseItem) mitem).getPrice()))));
				inv.setItem(mitem.getSlot(), Util.replace(mitem.getItem().clone(), placeholders));
				return;
			}
			ItemStack item = mitem.getItem().clone();
			Util.setOwner(item, player.getName());
			item = Util.replace(item, new Files.Pair<>("{exp}", NumberFormat.getNumberInstance().format(exp)), new Files.Pair<>("{player}", player.getName()));
			item = Util.replace(item, enchant.getPlaceholders(enchants));
			inv.setItem(mitem.getSlot(), item);
		});
	}
	
	private Enchant getMetaEnchant(Player player) {
		try {
			return enchantManager.getEnchant(player.getMetadata("pickaxeupgrades_enchant").get(0).asString());
		} catch(Exception ex) {
			return null;
		}
	}
}