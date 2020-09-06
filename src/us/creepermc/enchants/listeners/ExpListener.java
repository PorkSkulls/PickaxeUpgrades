package us.creepermc.enchants.listeners;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import us.creepermc.enchants.Core;
import us.creepermc.enchants.managers.PickaxeManager;
import us.creepermc.enchants.templates.XListener;
import us.creepermc.enchants.utils.Util;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExpListener extends XListener {
	PickaxeManager pickaxeManager;
	
	public ExpListener(Core core) {
		super(core);
	}
	
	@Override
	public void initialize() {
		deinitialize();
		
		pickaxeManager = getCore().getManager(PickaxeManager.class);
	}
	
	@Override
	public void deinitialize() {
		pickaxeManager = null;
	}
	
	@EventHandler
	public void mine(BlockBreakEvent event) {
		Player player = event.getPlayer();
		ItemStack pickaxe = player.getItemInHand();
		if(pickaxe == null || !Util.isPickaxe(pickaxe.getType())) return;
		player.setItemInHand(pickaxeManager.blockBreak(pickaxe));
	}
}