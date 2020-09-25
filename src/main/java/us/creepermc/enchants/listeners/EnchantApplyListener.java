package us.creepermc.enchants.listeners;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import us.creepermc.enchants.Core;
import us.creepermc.enchants.managers.EnchantManager;
import us.creepermc.enchants.objects.EffectEnchant;
import us.creepermc.enchants.templates.XListener;

import java.util.Objects;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class EnchantApplyListener extends XListener {
	EnchantManager manager;
	
	public EnchantApplyListener(Core core) {
		super(core);
	}
	
	@Override
	public void initialize() {
		deinitialize();
		
		manager = getCore().getManager(EnchantManager.class);
	}
	
	@Override
	public void deinitialize() {
		manager = null;
	}
	
	@EventHandler
	public void itemHeld(PlayerItemHeldEvent event) {
		Player player = event.getPlayer();
		ItemStack oldItem = player.getInventory().getItem(event.getPreviousSlot());
		ItemStack newItem = player.getInventory().getItem(event.getNewSlot());
		applyEnchantChanges(player, oldItem, newItem);
	}
	
	@EventHandler
	public void itemHeld(InventoryClickEvent event) {
		if(event.getInventory() == null) return;
		ItemStack oldItem;
		ItemStack newItem;
		Player player = (Player) event.getWhoClicked();
		if(event.getHotbarButton() != -1) {
			if(player.getInventory().getHeldItemSlot() != event.getHotbarButton() && (event.getRawSlot() < 36 || event.getRawSlot() > 44)) return;
			oldItem = player.getInventory().getItem(event.getHotbarButton());
			newItem = event.getCurrentItem();
			if(Objects.equals(player.getItemInHand(), newItem)) {
				newItem = oldItem;
				oldItem = event.getCurrentItem();
			}
		} else if(event.getInventory().getType() == InventoryType.CRAFTING && event.getRawSlot() >= 36 && event.getRawSlot() <= 44) {
			if(player.getInventory().getHeldItemSlot() != event.getRawSlot() - 36) return;
			oldItem = event.getCurrentItem();
			newItem = event.getCursor();
		} else if(Objects.equals(player.getItemInHand(), event.getCurrentItem())) {
			oldItem = event.getCurrentItem();
			newItem = null;
		} else return;
		if(Objects.equals(oldItem, newItem)) return;
		applyEnchantChanges(player, oldItem, newItem);
	}
	
	private void applyEnchantChanges(Player player, ItemStack oldItem, ItemStack newItem) {
		if((oldItem == null || oldItem.getType() == Material.AIR) && (newItem == null || newItem.getType() == Material.AIR)) return;
		if(oldItem != null && oldItem.getType() != Material.AIR) {
			manager.getEnchants(oldItem).entrySet().stream()
					.filter(entry -> entry.getKey() instanceof EffectEnchant && entry.getKey().isEnabled())
					.forEach(entry -> ((EffectEnchant) entry.getKey()).remove(player));
		}
		if(newItem != null && newItem.getType() != Material.AIR) {
			manager.getEnchants(newItem).entrySet().stream()
					.filter(entry -> entry.getKey() instanceof EffectEnchant && entry.getKey().isEnabled())
					.forEach(entry -> ((EffectEnchant) entry.getKey()).apply(player, entry.getValue()));
		}
	}
}
