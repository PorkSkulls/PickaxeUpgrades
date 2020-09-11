package us.creepermc.enchants.templates;

import lombok.Getter;
import org.bukkit.event.Listener;
import us.creepermc.enchants.Core;

@Getter
public abstract class XListener extends XManager implements Listener {
	public XListener(Core core) {
		super(core);
	}
}