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
import us.creepermc.enchants.objects.ItemPick;
import us.creepermc.enchants.templates.XInvManager;
import us.creepermc.enchants.utils.Files;
import us.creepermc.enchants.utils.Util;

import java.text.NumberFormat;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class EnchantsInvManager extends XInvManager {
	EnchantManager enchantManager;
	PickaxeManager pickaxeManager;
	PurchaseInvManager purchaseInvManager;
	
	public EnchantsInvManager(Core core) {
		super(core, "enchant_inventory");
	}
	
	@Override
	public void initialize() {
		deinitialize();
		super.initialize();
		
		enchantManager = getCore().getManager(EnchantManager.class);
		pickaxeManager = getCore().getManager(PickaxeManager.class);
		purchaseInvManager = getCore().getManager(PurchaseInvManager.class);
	}
	
	@Override
	public void deinitialize() {
		super.deinitialize();
		enchantManager = null;
		pickaxeManager = null;
		purchaseInvManager = null;
	}
	
	public void openInventory(Player player) {
		getCore().sendMsg(player, "OPEN_INVENTORY");
		Inventory copy = getCore().getServer().createInventory(null, getInventory().getInventory().getSize(), getInventory().getTitle());
		copy.setContents(getInventory().getInventory().getContents());
		ItemPick pickaxe = pickaxeManager.getItemPick(player.getItemInHand());
		getMenuItems().forEach(mitem -> {
			Enchant enchant = enchantManager.getEnchant(mitem.getId());
			if(enchant == null) {
				ItemStack item = mitem.getItem().clone();
				Util.setOwner(item, player.getName());
				item = Util.replace(item, new Files.Pair<>("{exp}", NumberFormat.getNumberInstance().format(pickaxe.getExp())), new Files.Pair<>("{player}", player.getName()));
				copy.setItem(mitem.getSlot(), item);
				return;
			}
			copy.setItem(mitem.getSlot(), Util.replace(mitem.getItem().clone(), enchant.getPlaceholders(pickaxe)));
		});
		player.openInventory(copy);
	}
	
	@Override
	public void menuItemClick(InventoryClickEvent event, MenuItem menuItem) {
		Enchant enchant = enchantManager.getEnchant(menuItem.getId());
		if(enchant == null) return;
		Player player = (Player) event.getWhoClicked();
		ItemPick pickaxe = pickaxeManager.getItemPick(player.getItemInHand());
		if(pickaxe.getEnchants().containsKey(enchant) && pickaxe.getEnchants().get(enchant) == enchant.getMaxLvl()) {
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
