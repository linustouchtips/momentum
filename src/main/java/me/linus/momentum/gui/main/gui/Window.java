package me.linus.momentum.gui.main.gui;

import me.linus.momentum.gui.theme.Theme;
import me.linus.momentum.managers.ModuleManager;
import me.linus.momentum.mixin.MixinInterface;
import me.linus.momentum.module.Module;
import me.linus.momentum.module.Module.Category;
import me.linus.momentum.module.modules.client.ClickGUI;
import me.linus.momentum.util.render.GUIUtil;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author bon & linustouchtips
 * @since 11/16/20
 */

public class Window implements MixinInterface {

	public int x;
	public int y;
	public int width;
	public int height = 0;

	public boolean ldown;
	public boolean rdown;
	public boolean dragging;
	public boolean opened = true;

	public int currentTheme;

	public int lastmX;
	public int lastmY;
	public String name;
	public Category category;
	public List<Module> modules;
	public static List<Window> windows = new ArrayList<>();

	public Window(Category category, int x, int y) {
		this.name = category.getName();
		this.x = x;
		this.y = y;
		this.category = category;
		this.modules = ModuleManager.getModulesInCategory(category);
	}

	public static void initGui() {
		windows.add(new Window(Category.COMBAT, 18, 22));
		windows.add(new Window(Category.PLAYER, 128, 22));
		windows.add(new Window(Category.MISC, 238, 22));
		windows.add(new Window(Category.MOVEMENT, 348, 22));
		windows.add(new Window(Category.RENDER, 458, 22));
		windows.add(new Window(Category.CLIENT, 568, 22));
		windows.add(new Window(Category.BOT, 568, 258));
	}

	public void drawGui(int mouseX, int mouseY, float partialTicks) {
		mouseListen();

		currentTheme = ClickGUI.theme.getValue();
		Theme current = Theme.getTheme(currentTheme);
		current.drawTitles(this.name, this.x, this.y);

		if (opened)
			current.drawModules(this.modules, this.x, this.y, mouseX, mouseY, partialTicks);

		reset();

		if (!ClickGUI.allowOverflow.getValue())
			resetOverflow();
	}

	public void resetOverflow() {
		int screenWidth = new ScaledResolution(mc).getScaledWidth();
		int screenHeight = new ScaledResolution(mc).getScaledHeight();

		if (this.width < 0) {
			if (this.x > screenWidth)
				this.x = screenWidth;

			if (this.x + this.width < 0)
				this.x = -this.width;
		}

		else {
			if (this.x < 0)
				this.x = 0;

			if (this.x + this.width > screenWidth)
				this.x = screenWidth - this.width;
		}

		if (this.y < 0)
			this.y = 0;

		if (this.y + this.height > screenHeight)
			this.y = screenHeight - this.height;
	}

	void mouseListen() {
		if (dragging) {
			x = GUIUtil.mX - (lastmX - x);
			y = GUIUtil.mY - (lastmY - y);
		}

		lastmX = GUIUtil.mX;
		lastmY = GUIUtil.mY;
	}

	void reset() {
		ldown = false;
		rdown = false;
	}

	public void lclickListen(int mouseX, int mouseY, int mouseButton) throws IOException {
		Theme current = Theme.getTheme(currentTheme);

		if (GUIUtil.mouseOver(x, y, x + current.getThemeWidth(), y + current.getThemeHeight()))
			dragging = true;
	}

	public void rclickListen(int mouseX, int mouseY, int mouseButton) throws IOException {
		Theme current = Theme.getTheme(currentTheme);

		if (GUIUtil.mouseOver(x, y, x + current.getThemeWidth(), y + current.getThemeHeight()))
			opened = !opened;
	}

	public void mouseWheelListen() {
		int scrollWheel = Mouse.getDWheel();

		for (Window windows : Window.windows) {
			if (scrollWheel < 0)
				windows.setY((int) (windows.getY() - ClickGUI.scrollSpeed.getValue()));
			else if (scrollWheel > 0)
				windows.setY((int) (windows.getY() + ClickGUI.scrollSpeed.getValue()));
		}
	}

	public void releaseListen(int mouseX, int mouseY, int state) {
		ldown = false;
		dragging = false;
	}

	public int getX() {
		return this.x;
	}

	public void setX(int newX) {
		this.x = newX;
	}

	public int getY() {
		return this.y;
	}

	public void setY(int newY) {
		this.y = newY;
	}
}