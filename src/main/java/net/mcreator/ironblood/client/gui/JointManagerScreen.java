package net.mcreator.ironblood.client.gui;

import org.valkyrienskies.core.impl.shadow.gy;
import org.valkyrienskies.core.impl.shadow.gx;
import org.valkyrienskies.core.impl.shadow.e;
import org.valkyrienskies.core.impl.shadow.c;
import org.valkyrienskies.core.impl.shadow.b;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;

import net.mcreator.ironblood.world.inventory.JointManagerMenu;
import net.mcreator.ironblood.network.JointManagerButtonMessage;
import net.mcreator.ironblood.IronbloodMod;

import java.util.HashMap;

import com.mojang.blaze3d.systems.RenderSystem;

public class JointManagerScreen extends AbstractContainerScreen<JointManagerMenu> {
	private final static HashMap<String, Object> guistate = JointManagerMenu.guistate;
	private final Level world;
	private final int x, y, z;
	private final Player entity;
	EditBox addjoint;
	ImageButton imagebutton_checkbox1;
	ImageButton imagebutton_xbox1;

	public JointManagerScreen(JointManagerMenu container, Inventory inventory, Component text) {
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
		addjoint.render(guiGraphics, mouseX, mouseY, partialTicks);
		this.renderTooltip(guiGraphics, mouseX, mouseY);
	}

	@Override
	protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int gx, int gy) {
		RenderSystem.setShaderColor(1, 1, 1, 1);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();

		// Render the text box background texture (moved 4 pixels left and up 3 pixels total)
		guiGraphics.blit(new ResourceLocation("ironblood:textures/screens/outlinejointtext.png"),
				this.leftPos - 4, this.topPos - 3, 0, 0, 122, 22, 122, 22);

		RenderSystem.disableBlend();
	}

	@Override
	public boolean keyPressed(int key, int b, int c) {
		if (key == 256) {
			this.minecraft.player.closeContainer();
			return true;
		}
		if (addjoint.isFocused())
			return addjoint.keyPressed(key, b, c);
		return super.keyPressed(key, b, c);
	}

	@Override
	public void containerTick() {
		super.containerTick();
		addjoint.tick();
	}

	@Override
	public void resize(Minecraft minecraft, int width, int height) {
		String addjointValue = addjoint.getValue();
		super.resize(minecraft, width, height);
		addjoint.setValue(addjointValue);
	}

	@Override
	protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
	}

	@Override
	public void init() {
		super.init();
		addjoint = new EditBox(this.font, this.leftPos + 2, this.topPos + 3, 118, 18, Component.translatable("gui.ironblood.joint_manager.addjoint"));
		addjoint.setMaxLength(32767);
		addjoint.setBordered(false); // Remove default border since we're using custom texture
		guistate.put("text:addjoint", addjoint);
		this.addWidget(this.addjoint);

		// Checkbox button (Add joint)
		imagebutton_checkbox1 = new ImageButton(this.leftPos + 129, this.topPos + -1, 20, 20, 0, 0, 20,
				new ResourceLocation("ironblood:textures/screens/atlas/imagebutton_checkbox1.png"), 20, 40, e -> {
			String jointText = addjoint.getValue().trim();
			if (!jointText.isEmpty()) {
				// Send packet to server to add joint
				IronbloodMod.PACKET_HANDLER.sendToServer(
						new JointManagerButtonMessage(
								new net.minecraft.core.BlockPos(x, y, z),
								jointText,
								true // true = add
						)
				);
			}
		});
		guistate.put("button:imagebutton_checkbox1", imagebutton_checkbox1);
		this.addRenderableWidget(imagebutton_checkbox1);

		// Xbox button (Remove joint)
		imagebutton_xbox1 = new ImageButton(this.leftPos + 156, this.topPos + -1, 20, 20, 0, 0, 20,
				new ResourceLocation("ironblood:textures/screens/atlas/imagebutton_xbox1.png"), 20, 40, e -> {
			String jointText = addjoint.getValue().trim();
			if (!jointText.isEmpty()) {
				// Send packet to server to remove joint
				IronbloodMod.PACKET_HANDLER.sendToServer(
						new JointManagerButtonMessage(
								new net.minecraft.core.BlockPos(x, y, z),
								jointText,
								false // false = remove
						)
				);
			}
		});
		guistate.put("button:imagebutton_xbox1", imagebutton_xbox1);
		this.addRenderableWidget(imagebutton_xbox1);
	}
}