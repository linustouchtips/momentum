package me.linus.momentum.module.modules.client;

import me.linus.momentum.gui.main.hud.HUD;
import me.linus.momentum.module.Module;
import me.linus.momentum.setting.checkbox.Checkbox;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author bon & linustouchtips
 * @since 12/01/202
 */

public class HUDEditor extends Module {
	public HUDEditor() {
		super("HUD", Category.CLIENT, "The in-game hud editor");
	}

	public static Checkbox allowOverflow = new Checkbox("Allow Overflow", false);
	public static Checkbox colorSync = new Checkbox("Color Sync", true);

	public void setup() {
		addSetting(allowOverflow);
		addSetting(colorSync);
	}
	
	public static HUD hudEditor = new HUD();
	
	@Override
	public void onEnable() {
		if (nullCheck())
			return;

		super.onEnable();
		mc.displayGuiScreen(hudEditor);
	}
}
	

