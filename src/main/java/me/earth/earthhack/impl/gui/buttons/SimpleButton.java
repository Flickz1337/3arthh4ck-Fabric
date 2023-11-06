package me.earth.earthhack.impl.gui.buttons;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.util.Identifier;
public class SimpleButton extends ButtonWidget
{
    protected static final Identifier LOCATION =
            new Identifier("earthhack:textures/gui/gui_textures.png");

    protected final int textureX;
    protected final int textureY;
    protected final int hoveredX;
    protected final int hoveredY;

    public SimpleButton(int buttonID,
                        int xPos,
                        int yPos,
                        int textureX,
                        int textureY,
                        int hoveredX,
                        int hoveredY)
    {
        super(buttonID, xPos, yPos, 20, 20, "");
        this.textureX = textureX;
        this.textureY = textureY;
        this.hoveredX = hoveredX;
        this.hoveredY = hoveredY;
    }

    public void onClick(Screen parent, int id)
    {
        /* Can be implemented by the button. */
    }

    public void drawButton(MinecraftClient mc, int mouseX, int mouseY, float partialTicks)
    {
        if (this.visible)
        {
            this.hovered = mouseX >= this.x
                    && mouseY >= this.y
                    && mouseX < this.x + this.width
                    && mouseY < this.y + this.height;

            mc.getTextureManager().bindTexture(LOCATION);
            GlStateManager._clearColor(1.0F, 1.0F, 1.0F, 1.0F);
            this.drawButton(mc,
                    this.x,
                    this.y,
                    this.hovered ? hoveredX : textureX,
                    this.hovered ? hoveredY : textureY,
                    this.width,
                    this.height);
        }
    }
}