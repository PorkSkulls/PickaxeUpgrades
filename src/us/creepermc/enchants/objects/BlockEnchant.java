package us.creepermc.enchants.objects;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public abstract class BlockEnchant extends ChanceEnchant {
	
	public BlockEnchant(YamlConfiguration config, String id) {
		super(config, id);
	}
	
	public abstract void apply(Player player, BlockBreakEvent event, int level);
	
	protected void simplifyItems(List<ItemStack> items) {
		Map<MaterialData, Integer> amounts = new HashMap<>();
		items.forEach(item -> amounts.put(item.getData(), amounts.getOrDefault(item.getData(), 0) + item.getAmount()));
		items.clear();
		amounts.forEach((type, amount) -> {
			while(amount > 0) {
				items.add(new ItemStack(type.getItemType(), Math.min(amount, type.getItemType().getMaxStackSize()), type.getData()));
				amount -= type.getItemType().getMaxStackSize();
			}
		});
	}
}
