package us.creepermc.enchants.cmds;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.creepermc.enchants.Core;
import us.creepermc.enchants.managers.EnchantManager;
import us.creepermc.enchants.objects.Enchant;
import us.creepermc.enchants.templates.XCommand;
import us.creepermc.enchants.utils.Files;
import us.creepermc.enchants.utils.Util;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class EnchanterCmd extends XCommand {
	EnchantManager enchantManager;
	
	public EnchanterCmd(Core core) {
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
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		if(!sender.hasPermission("pickaxeupgrades.admin")) return;
		if(args.length > 0 && args[0].equalsIgnoreCase("reload")) {
			getCore().initConfig();
			getCore().sendMsg(sender, "RELOADED");
			return;
		} else if(args.length > 0 && args[0].equalsIgnoreCase("add") && sender instanceof Player) {
			if(args.length < 2) {
				getCore().sendMsg(sender, "USAGE_ADD");
				return;
			}
			Enchant enchant = enchantManager.getEnchant(args[1]);
			if(enchant == null) {
				getCore().sendMsg(sender, "INVALID_ENCHANT", args[1]);
				return;
			}
			Player player = (Player) sender;
			if(player.getItemInHand() == null || !Util.isPickaxe(player.getItemInHand().getType())) {
				getCore().sendMsg(player, "CANNOT_APPLY");
				return;
			}
			int level = args.length > 2 && Util.isInt(args[2]) ? Math.max(Math.min(Integer.parseInt(args[2]), enchant.getMaxLvl()), enchant.getMinLvl()) : enchant.getMinLvl();
			enchant.apply(player.getItemInHand(), enchantManager.getEnchants(player.getItemInHand()), level);
			player.updateInventory();
			getCore().sendMsg(player, "ADDED_ENCHANT",
					new Files.Pair<>("{enchant}", enchant.getName()),
					new Files.Pair<>("{level}", String.valueOf(level))
			);
			return;
		} else if(args.length > 0 && args[0].equalsIgnoreCase("remove") && sender instanceof Player) {
			if(args.length < 2) {
				getCore().sendMsg(sender, "USAGE_REMOVE");
				return;
			}
			Enchant enchant = enchantManager.getEnchant(args[1]);
			if(enchant == null) {
				getCore().sendMsg(sender, "INVALID_ENCHANT", args[1]);
				return;
			}
			Player player = (Player) sender;
			if(!enchantManager.getEnchants(player.getItemInHand()).containsKey(enchant)) {
				getCore().sendMsg(player, "DOESNT_HAVE");
				return;
			}
			enchant.remove(player.getItemInHand());
			player.updateInventory();
			getCore().sendMsg(player, "REMOVED_ENCHANT", new Files.Pair<>("{enchant}", enchant.getName()));
			return;
		}
		getCore().sendMsg(sender, "USAGE");
	}
}