package net.mcreator.ironblood.client.gui;

import org.valkyrienskies.core.impl.shadow.gy;
import org.valkyrienskies.core.impl.shadow.gx;
import org.valkyrienskies.core.impl.shadow.c;
import org.valkyrienskies.core.impl.shadow.b;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;

import net.mcreator.ironblood.world.inventory.ShipnamerassemblerMenu;

import java.util.HashMap;

import com.mojang.blaze3d.systems.RenderSystem;

public class ShipnamerassemblerScreen extends AbstractContainerScreen<ShipnamerassemblerMenu> {
	private final static HashMap<String, Object> guistate = ShipnamerassemblerMenu.guistate;
	private final Level world;
	private final int x, y, z;
	private final Player entity;
	EditBox AssembledName;

	public ShipnamerassemblerScreen(ShipnamerassemblerMenu container, Inventory inventory, Component text) {
		super(container, inventory, text);
		this.world = container.world;
		this.x = container.x;
		this.y = container.y;
		this.z = container.z;
		this.entity = container.entity;
		this.imageWidth = 176;
		this.imageHeight = 166;
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(guiGraphics);
		super.render(guiGraphics, mouseX, mouseY, partialTicks);
		AssembledName.render(guiGraphics, mouseX, mouseY, partialTicks);
		this.renderTooltip(guiGraphics, mouseX, mouseY);
	}

	@Override
	protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int gx, int gy) {
		RenderSystem.setShaderColor(1, 1, 1, 1);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableBlend();
	}

	@Override
	public boolean keyPressed(int key, int b, int c) {
		if (key == 256) {
			this.minecraft.player.closeContainer();
			return true;
		}
		if (AssembledName.isFocused())
			return AssembledName.keyPressed(key, b, c);
		return super.keyPressed(key, b, c);
	}

	@Override
	public void containerTick() {
		super.containerTick();
		AssembledName.tick();
	}

	@Override
	public void resize(Minecraft minecraft, int width, int height) {
		String AssembledNameValue = AssembledName.getValue();
		super.resize(minecraft, width, height);
		AssembledName.setValue(AssembledNameValue);
	}

	@Override
	protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
	}

	@Override
	public void init() {
		super.init();
		AssembledName = new EditBox(this.font, this.leftPos + 28, this.topPos + 67, 118, 18, Component.translatable("gui.ironblood.shipnamerassembler.AssembledName")) {
			@Override
			public void insertText(String text) {
				super.insertText(text);
				if (getValue().isEmpty())
					setSuggestion(Component.translatable("gui.ironblood.shipnamerassembler.AssembledName").getString());
				else
					setSuggestion(null);
			}

			@Override
			public void moveCursorTo(int pos) {
				super.moveCursorTo(pos);
				if (getValue().isEmpty())
					setSuggestion(Component.translatable("gui.ironblood.shipnamerassembler.AssembledName").getString());
				else
					setSuggestion(null);
			}
		};
		AssembledName.setSuggestion(Component.translatable("gui.ironblood.shipnamerassembler.AssembledName").getString());
		AssembledName.setMaxLength(32767);
		guistate.put("text:AssembledName", AssembledName);
		this.addWidget(this.AssembledName);
	}
}
