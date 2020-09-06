package us.creepermc.enchants.listeners;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import us.creepermc.enchants.Core;
import us.creepermc.enchants.managers.EnchantManager;
import us.creepermc.enchants.objects.BlockEnchant;
import us.creepermc.enchants.objects.ChanceEnchant;
import us.creepermc.enchants.objects.Enchant;
import us.creepermc.enchants.templates.XListener;
import us.creepermc.enchants.utils.Util;

import java.util.Map;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class EnchantListener extends XListener {
	EnchantManager enchantManager;
	
	public EnchantListener(Core core) {
		super(core);
	}
	
	@Override
	public void initialize() {
		enchantManager = getCore().getManager(EnchantManager.class);
	}
	
	@Override
	public void deinitialize() {
		enchantManager = null;
	}
	
	@EventHandler
	public void blockBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		if(player.getItemInHand() == null || !Util.isPickaxe(player.getItemInHand().getType())) return;
		Map<Enchant, Integer> enchants = enchantManager.getEnchants(player.getItemInHand());
		enchants.forEach((enchant, level) -> {
			if(!(enchant instanceof BlockEnchant)) return;
			if(!((ChanceEnchant) enchant).pass(level)) return;
			((BlockEnchant) enchant).apply(player, event, level);
		});
	}
}