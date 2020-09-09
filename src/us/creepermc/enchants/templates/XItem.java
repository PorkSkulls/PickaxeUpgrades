package us.creepermc.enchants.templates;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import us.creepermc.enchants.Core;
import us.creepermc.enchants.utils.Files;
import us.creepermc.enchants.utils.NBTEditor;
import us.creepermc.enchants.utils.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class XItem extends XManager {
	final String path;
	final List<String> placeholders;
	ItemStack item;
	Files.Pair<Pattern, List<Pattern>> regex;
	
	public XItem(Core core, String path, List<String> placeholders) {
		super(core);
		this.path = path;
		this.placeholders = placeholders;
	}
	
	@Override
	public void initialize() {
		deinitialize();
		super.initialize();
		
		item = Util.getItem(getCore().getConfig(), path);
		regex = getRegex(item, placeholders);
	}
	
	@Override
	public void deinitialize() {
		item = null;
		regex = null;
	}
	
	private String escRegex(String str) {
		return str.replaceAll("[{}()\\[\\].+*?^$\\\\|]", "\\\\$0");
	}
	
	private Files.Pair<Pattern, List<Pattern>> getRegex(ItemStack item, List<String> placeholders) {
		String name = item.getItemMeta().getDisplayName();
		for(String ph : placeholders) name = name.replace(ph, "%s");
		name = escRegex(name).replace("%s", ".*");
		List<String> lore = item.getItemMeta().hasLore() ? item.getItemMeta().getLore() : new ArrayList<>();
		for(int i = 0; i < lore.size(); i++) {
			for(String ph : placeholders) lore.set(i, lore.get(i).replace(ph, "%s"));
			lore.set(i, escRegex(lore.get(i)).replace("%s", ".*"));
		}
		return new Files.Pair<>(Pattern.compile(name), lore.stream().map(Pattern::compile).collect(Collectors.toList()));
	}
	
	public String getPlaceholder(ItemStack item, String nbtid, String placeholder) {
		if(item == null || item.getType() != this.item.getType() || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return "";
		if(!NBTEditor.get(item, nbtid).isEmpty()) return NBTEditor.get(item, nbtid);
		ItemMeta meta = item.getItemMeta();
		if(!regex.getKey().matcher(meta.getDisplayName()).matches()) return "";
		if(regex.getValue().size() != 0 && !meta.hasLore() || meta.hasLore() && regex.getValue().size() != meta.getLore().size()) return "";
		boolean finish = true;
		for(int i = 0; i < regex.getValue().size(); i++) if(!regex.getValue().get(i).matcher(meta.getLore().get(i)).matches()) finish = false;
		return !finish ? "" : Util.getPlaceholder(item, this.item, placeholder);
	}
}