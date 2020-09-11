package us.creepermc.enchants.cmds;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import us.creepermc.enchants.Core;
import us.creepermc.enchants.managers.StorageManager;
import us.creepermc.enchants.objects.EnergyVoucher;
import us.creepermc.enchants.templates.XCommand;
import us.creepermc.enchants.utils.Files;
import us.creepermc.enchants.utils.Util;

import java.text.NumberFormat;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class EnergyCmd extends XCommand {
	EnergyVoucher voucher;
	StorageManager manager;
	
	public EnergyCmd(Core core) {
		super(core);
	}
	
	@Override
	public void initialize() {
		deinitialize();
		
		voucher = getCore().getManager(EnergyVoucher.class);
		manager = getCore().getManager(StorageManager.class);
	}
	
	@Override
	public void deinitialize() {
		voucher = null;
		manager = null;
	}
	
	@Override
	public void execute(CommandSender sender, String[] args) {
		if(args.length > 0 && args[0].equalsIgnoreCase("reload") && sender.hasPermission("pickaxeupgrades.admin")) {
			getCore().initConfig();
			getCore().sendMsg(sender, "RELOADED");
			return;
		} else if(args.length > 0 && (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("take") || args[0].equalsIgnoreCase("set")) && sender.hasPermission("pickaxeupgrades.admin")) {
			if(args.length < 3) {
				getCore().sendMsg(sender, "USAGE_" + args[0].toUpperCase());
				return;
			}
			Player target = getCore().getServer().getPlayer(args[1]);
			if(target == null) {
				getCore().sendMsg(sender, "OFFLINE", args[1]);
				return;
			}
			int amount = Util.isDouble(args[2]) ? (int) Double.parseDouble(args[2]) : -1;
			if(amount <= 0) {
				getCore().sendMsg(sender, "INVALID_AMOUNT", args[2]);
				return;
			}
			switch(args[0].toLowerCase()) {
				case "give":
					manager.addExp(target.getUniqueId(), amount);
					break;
				case "take":
					manager.removeExp(target.getUniqueId(), amount);
					break;
				case "set":
					manager.setExp(target.getUniqueId(), amount);
					break;
			}
			if(!sender.equals(target))
				getCore().sendMsg(sender, args[0].toUpperCase(), new Files.Pair<>("{player}", target.getName()), new Files.Pair<>("{amount}", NumberFormat.getNumberInstance().format(amount)));
			getCore().sendMsg(target, args[0].toUpperCase() + "_OTHER", new Files.Pair<>("{amount}", NumberFormat.getNumberInstance().format(amount)));
			return;
		} else if(args.length > 0 && args[0].equalsIgnoreCase("withdraw") && sender instanceof Player) {
			Player player = (Player) sender;
			if(args.length < 2) {
				getCore().sendMsg(sender, "USAGE_WITHDRAW");
				return;
			}
			int amount = Util.isDouble(args[1]) ? (int) Double.parseDouble(args[1]) : -1;
			if(amount <= 0) {
				getCore().sendMsg(sender, "INVALID_AMOUNT", args[1]);
				return;
			}
			long exp = manager.getExp(player.getUniqueId());
			if(exp < amount) {
				getCore().sendMsg(sender, "NOT_ENOUGH_EXP");
				return;
			}
			manager.removeExp(player.getUniqueId(), amount);
			ItemStack item = voucher.getVoucher(amount);
			getCore().sendMsg(player, "WITHDRAWN", NumberFormat.getNumberInstance().format(amount));
			if(player.getInventory().firstEmpty() != -1) player.getInventory().addItem(item);
			else {
				player.getWorld().dropItemNaturally(player.getLocation(), item);
				getCore().sendMsg(player, "WITHDRAW_FULL");
			}
			return;
		}
		if(!(sender instanceof Player)) {
			getCore().sendMsg(sender, "USAGE_ENERGY");
			return;
		}
		Player player = (Player) sender;
		long amount = manager.getExp(player.getUniqueId());
		getCore().sendMsg(sender, "ENERGY_AMOUNT", NumberFormat.getNumberInstance().format(amount));
	}
}