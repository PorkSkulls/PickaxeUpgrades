package us.creepermc.enchants.listeners;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import us.creepermc.enchants.Core;
import us.creepermc.enchants.managers.StorageManager;
import us.creepermc.enchants.templates.XListener;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExpListener extends XListener {
	StorageManager storageManager;
	
	public ExpListener(Core core) {
		super(core);
	}
	
	@Override
	public void initialize() {
		storageManager = getCore().getManager(StorageManager.class);
	}
	
	@Override
	public void deinitialize() {
		storageManager = null;
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void mine(BlockBreakEvent event) {
		storageManager.blockBreak(event.getPlayer().getUniqueId());
	}
}