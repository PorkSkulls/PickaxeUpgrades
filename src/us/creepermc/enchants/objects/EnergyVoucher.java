package us.creepermc.enchants.objects;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import us.creepermc.enchants.Core;
import us.creepermc.enchants.managers.StorageManager;
import us.creepermc.enchants.templates.XItem;
import us.creepermc.enchants.utils.Files;
import us.creepermc.enchants.utils.NBTEditor;
import us.creepermc.enchants.utils.Util;

import java.text.NumberFormat;
import java.util.Collections;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class EnergyVoucher extends XItem {
	StorageManager manager;
	
	public EnergyVoucher(Core core) {
		super(core, "energy-voucher", Collections.singletonList("{amount}"));
	}
	
	@Override
	public void initialize() {
		deinitialize();
		super.initialize();
		
		manager = getCore().getManager(StorageManager.class);
	}
	
	@Override
	public void deinitialize() {
		super.deinitialize();
		manager = null;
	}
	
	public void redeem(Player player, int money) {
		if(player.getItemInHand().getAmount() > 1) player.getItemInHand().setAmount(player.getItemInHand().getAmount() - 1);
		else player.setItemInHand(null);
		manager.addExp(player.getUniqueId(), money);
	}
	
	public ItemStack getVoucher(int exp) {
		ItemStack item = getItem().clone();
		item = Util.replace(item, new Files.Pair<>("{amount}", NumberFormat.getNumberInstance().format(exp)));
		item = NBTEditor.set(item, "energyvoucher_amount", String.valueOf(exp));
		return item;
	}
	
	public int getAmount(ItemStack item) {
		String amount = getPlaceholder(item, "energyvoucher_amount", "{amount}");
		return amount.isEmpty() ? -1 : Util.isInt(amount.replace(",", "")) ? Integer.parseInt(amount.replace(",", "")) : -1;
	}
}