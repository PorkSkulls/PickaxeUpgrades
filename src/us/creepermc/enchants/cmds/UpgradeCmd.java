package us.creepermc.enchants.cmds;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.creepermc.enchants.Core;
import us.creepermc.enchants.managers.EnchantsInvManager;
import us.creepermc.enchants.managers.PickaxeManager;
import us.creepermc.enchants.templates.XCommand;
import us.creepermc.enchants.utils.Util;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpgradeCmd extends XCommand {
	EnchantsInvManager invManager;
	PickaxeManager pickaxeManager;
	
	public UpgradeCmd(Core core) {
		super(core);
	}
	
	@Override
	public void initialize() {
		deinitialize();
		
		invManager = getCore().getManager(EnchantsInvManager.class);
		pickaxeManager = getCore().getManager(PickaxeManager.class);
	}
	
	@Override
	public void deinitialize() {
		invManager = null;
		pickaxeManager = null;
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		if(args.length > 0 && args[0].equalsIgnoreCase("reload") && sender.hasPermission("pickaxeupgrades.admin")) {
			getCore().initConfig();
			getCore().sendMsg(sender, "RELOADED");
			return;
		}
		if(!(sender instanceof Player)) return;
		Player player = (Player) sender;
		if(player.getItemInHand() == null || !Util.isPickaxe(player.getItemInHand().getType())) {
			getCore().sendMsg(player, "NOT_PICKAXE");
			return;
		}
		pickaxeManager.initializePickaxe(player);
		invManager.openInventory(player);
	}
}
