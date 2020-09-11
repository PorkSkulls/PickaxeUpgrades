package us.creepermc.enchants.listeners;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import us.creepermc.enchants.Core;
import us.creepermc.enchants.objects.EnergyVoucher;
import us.creepermc.enchants.templates.XListener;

import java.text.NumberFormat;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class VoucherListener extends XListener {
	EnergyVoucher manager;
	
	public VoucherListener(Core core) {
		super(core);
	}
	
	@Override
	public void initialize() {
		manager = getCore().getManager(EnergyVoucher.class);
	}
	
	@Override
	public void deinitialize() {
		manager = null;
	}
	
	@EventHandler
	public void interact(PlayerInteractEvent event) {
		int amount = manager.getAmount(event.getItem());
		if(amount <= 0) return;
		Player player = event.getPlayer();
		manager.redeem(player, amount);
		getCore().sendMsg(player, "REDEEMED", NumberFormat.getNumberInstance().format(amount));
	}
}