package us.creepermc.enchants;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import us.creepermc.enchants.templates.XManager;
import us.creepermc.enchants.utils.Files;
import us.creepermc.enchants.utils.Util;
import us.creepermc.enchants.utils.XSound;

import java.io.File;
import java.util.*;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Core extends JavaPlugin {
	final List<XManager> managers = new ArrayList<>();
	final List<Files.XFile<?>> send = new ArrayList<>();
	String allowed;
	
	boolean usingPAPI;
	
	@Override
	public void onEnable() {
		send.addAll(Arrays.asList(new Files.Messages(this), new Files.Sounds(this)));
		
		initConfig();
		
		Util.registerHooks(this);
		usingPAPI = getServer().getPluginManager().isPluginEnabled("PlaceholderAPI");
	}
	
	@Override
	public void onDisable() {
		getServer().getScheduler().cancelTasks(this);
		
		managers.forEach(XManager::deinitialize);
		managers.clear();
		send.stream().map(Files.XFile::getStorage).forEach(Map::clear);
		send.clear();
		allowed = null;
		usingPAPI = false;
	}
	
	public void initConfig() {
		if(!new File(getDataFolder(), "config.yml").exists()) saveDefaultConfig();
		reloadConfig();
		managers.forEach(XManager::initialize);
		send.forEach(Files.XFile::reloadStorage);
		
		Map<String, String> defMsgs = new HashMap<>();
		Map<String, Files.SSound> defSnds = new HashMap<>();
		defMsgs.put("PREFIX", "&6" + getDescription().getName() + " &8\u00BB &7");
		putMsg(defMsgs, defSnds, "RELOADED", "You have updated the config file(s)", XSound.FIZZ);
		putMsg(defMsgs, defSnds, "USAGE", "Use &c/enchanter [reload, add, remove]", XSound.FIZZ);
		putMsg(defMsgs, defSnds, "USAGE_ADD", "Use &c/enchanter add <enchant> [level]", XSound.FIZZ);
		putMsg(defMsgs, defSnds, "USAGE_REMOVE", "Use &c/enchanter remove <enchant>", XSound.FIZZ);
		putMsg(defMsgs, defSnds, "OPEN_PURCHASE_INVENTORY", "", XSound.ORB_PICKUP);
		putMsg(defMsgs, defSnds, "OPEN_INVENTORY", "", XSound.ORB_PICKUP);
		putMsg(defMsgs, defSnds, "NOT_ENOUGH_EXP", "Your pickaxe does not have enough experience for that", XSound.FIZZ);
		putMsg(defMsgs, defSnds, "INVALID_ENCHANT", "Could not find an enchant called %s", XSound.FIZZ);
		putMsg(defMsgs, defSnds, "PURCHASED", "You have purchased {level} level(s) of {enchant}", XSound.ORB_PICKUP);
		putMsg(defMsgs, defSnds, "CANNOT_APPLY", "You cannot apply that enchantment to that item", XSound.FIZZ);
		putMsg(defMsgs, defSnds, "DOESNT_HAVE", "The item in your hand doesn't have that enchantment", XSound.FIZZ);
		putMsg(defMsgs, defSnds, "ADDED_ENCHANT", "You have added {level} {enchant} level(s) to your pickaxe", XSound.ORB_PICKUP);
		putMsg(defMsgs, defSnds, "REMOVED_ENCHANT", "You have removed {enchant} from your pickaxe", XSound.ORB_PICKUP);
		putMsg(defMsgs, defSnds, "NOT_PICKAXE", "The item in your hand is not a pickaxe", XSound.FIZZ);
		putMsg(defMsgs, defSnds, "MAX_LEVEL", "Your pickaxe has reached the max level of that enchant", XSound.FIZZ);
		putMsg(defMsgs, defSnds, "FULL_INVENTORY", "Your inventory was full so the rest of your items disappeared", XSound.FIZZ);
		getSend(Files.Messages.class).load(defMsgs);
		getSend(Files.Sounds.class).load(defSnds);
	}
	
	public void sendMsg(CommandSender sender, String key, String... replace) {
		send.forEach(xfile -> xfile.send(sender, key, replace));
	}
	
	@SafeVarargs
	public final void sendMsg(CommandSender sender, String key, Files.Pair<String, String>... replace) {
		send.forEach(xfile -> xfile.send(sender, key, replace));
	}
	
	public void sendMsg(CommandSender sender, String key, List<Files.Pair<String, String>> replace) {
		send.forEach(xfile -> xfile.send(sender, key, replace));
	}
	
	public void sendMsg(CommandSender sender, String key) {
		send.forEach(xfile -> xfile.send(sender, key, new String[0]));
	}
	
	public <T> T getManager(Class<T> clazz) {
		return (T) managers.stream().filter(manager -> manager.getClass().equals(clazz)).findFirst().orElse(null);
	}
	
	public <T> T getSend(Class<T> clazz) {
		return (T) send.stream().filter(xfile -> xfile.getClass().equals(clazz)).findFirst().orElse(null);
	}
	
	private void putMsg(Map<String, String> msgs, Map<String, Files.SSound> sounds, String key, String msg, XSound sound) {
		msgs.put(key, msg);
		sounds.put(key, new Files.SSound(sound.bukkitSound()));
	}
}
