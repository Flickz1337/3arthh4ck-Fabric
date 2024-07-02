package me.earth.earthhack.impl.util.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import me.earth.earthhack.api.cache.ModuleCache;
import me.earth.earthhack.api.util.TextUtil;
import me.earth.earthhack.api.util.interfaces.Globals;
import me.earth.earthhack.impl.managers.Managers;
import me.earth.earthhack.impl.managers.render.TextRenderer;
import me.earth.earthhack.impl.modules.Caches;
import me.earth.earthhack.impl.modules.client.editor.HudEditor;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

import java.awt.*;

public class Render2DUtil implements Globals {

    public static double getScreenScale() {
        return mc.getWindow().getScaleFactor();
    }
    public static int getScreenWidth() {
        return mc.getWindow().getWidth();
    }
    public static int getScreenHeight() {
        return mc.getWindow().getHeight();
    }
    private static final ModuleCache<HudEditor> HUD_EDITOR =
            Caches.getModule(HudEditor.class);


    public static void drawBlurryRect(MatrixStack matrix, float x, float y, float x1, float y1, int intensity, float size) {
        drawRect(
                matrix,
                (int) x,
                (int) y,
                (int) x1,
                (int) y1, new Color(50, 50, 50, 50).getRGB());
        blurArea(
                (int) x,
                (int) y,
                (int) x1 - (int) x,
                (int) y1 - (int) y,
                intensity, size, size);
    }

    public static void drawRect(MatrixStack matrix, float startX, float startY, float endX, float endY, int color) {
        drawRect(matrix, startX, startY, endX, endY, color, 0);
    }

    public static void drawRect(MatrixStack matrix, float startX, float startY, float endX, float endY, int color, int zLevel) {
        if (Managers.TEXT.usingCustomFont()) {
            TextRenderer.FONTS.drawRect(startX, startY, endX, endY, color);
        } else {
            BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            Matrix4f posMatrix = matrix.peek().getPositionMatrix();
            RenderSystem.setShader(GameRenderer::getPositionColorProgram);
            bufferBuilder.vertex(posMatrix, startX, endY, zLevel).color(color);
            bufferBuilder.vertex(posMatrix, endX, endY, zLevel).color(color);
            bufferBuilder.vertex(posMatrix, endX, startY, zLevel).color(color);
            bufferBuilder.vertex(posMatrix, startX, startY, zLevel).color(color);
            BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        }
    }

    public static void drawQuarterCircle(MatrixStack matrix, float x, float y, float radius, int color, int position) {
        drawRect(matrix, x - radius, y - radius, x + radius, y + radius, color);
        // use shader
    }

    public static void drawBorderedRect(MatrixStack matrix, float x, float y, float x2, float y2, float lineSize, int color, int borderColor) {
        drawRect(matrix, x, y, x2, y2, color);
        drawRect(matrix, x, y, x + lineSize, y2, borderColor);
        drawRect(matrix, x2 - lineSize, y, x2, y2, borderColor);
        drawRect(matrix, x, y2 - lineSize, x2, y2, borderColor);
        drawRect(matrix, x, y, x2, y + lineSize, borderColor);
    }

    public static void progressBar(MatrixStack matrix, float startX, float endX, float y, float radius, int color) {
        // y is the horizontal middle of the bar
        float startY = y - radius / 2, endY = y + radius / 2;
        drawRect(matrix, startX - radius, startY, endX + radius, endY, color);

        drawQuarterCircle(matrix, startX - radius, y, radius / 2 - 0.3f, color, 3);
        drawQuarterCircle(matrix, startX - radius, y, radius / 2 - 0.3f, color, 4);
        drawQuarterCircle(matrix, endX + radius, y, radius / 2 - 0.3f, color, 1);
        drawQuarterCircle(matrix, endX + radius, y, radius / 2 - 0.3f, color, 2);
    }

    public static void roundedRect(MatrixStack matrix, float startX, float startY, float endX, float endY, float radius, int color) {
        if (Managers.TEXT.usingCustomFont()) {
            TextRenderer.FONTS.drawRoundedRect(startX, startY, endX, endY, radius, color);
        } else {
            drawRect(matrix, startX, startY - radius, endX, endY + radius, color);
            drawRect(matrix, startX - radius, startY, startX, endY, color);
            drawRect(matrix, endX, startY, endX + radius, endY, color);

            drawQuarterCircle(matrix, endX, endY, radius - 0.2f, color, 1);
            drawQuarterCircle(matrix, endX, startY, radius - 0.2f, color, 2);
            drawQuarterCircle(matrix, startX, startY, radius - 0.2f, color, 3);
            drawQuarterCircle(matrix, startX, endY, radius - 0.2f, color, 4);
        }
    }

    public static void drawCheckMark(MatrixStack matrix, float x, float y, int width, int color) {
        drawLine(matrix, x + width - 6.5f, y + 3f, x + width - 11.5f, y + 10f, 0.8f, color);
        drawLine(matrix, x + width - 11.5f, y + 10f, x + width - 13.5f, y + 8, 0.8f, color);
    }

    public static void drawLine(MatrixStack matrix, float x, float y, float x1, float y1, float lineWidth, int color) {
        if (Managers.TEXT.usingCustomFont()) {
            TextRenderer.FONTS.drawLine(x, y, x1, y1, lineWidth, color);
        } else {
            float directionX = x1 - x;
            float directionY = y1 - y;

            float lineLength = (float) Math.sqrt(directionX * directionX + directionY * directionY);
            float normalizedX = directionX / lineLength;
            float normalizedY = -(directionY / lineLength);

            float width = lineWidth / 2.0f;

            BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            Matrix4f posMatrix = matrix.peek().getPositionMatrix();
            RenderSystem.setShader(GameRenderer::getPositionColorProgram);
            bufferBuilder.vertex(posMatrix, x + normalizedY * width, y + normalizedX * width, 0.0F).color(color);
            bufferBuilder.vertex(posMatrix, x1 + normalizedY * width, y1 + normalizedX * width, 0.0F).color(color);
            bufferBuilder.vertex(posMatrix, x1 - normalizedY * width, y1 - normalizedX * width, 0.0F).color(color);
            bufferBuilder.vertex(posMatrix, x - normalizedY * width, y - normalizedX * width, 0.0F).color(color);
            BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        }
    }

    public static void drawCheckeredBackground(MatrixStack matrix, float x, float y, float x2, float y2) {
        drawRect(matrix, x, y, x2, y2, 0xFFFFFFFF);

        for (boolean offset = false; y < y2; y++) {
            for (float x1 = x + ((offset = !offset) ? 1 : 0); x1 < x2; x1 += 2) {
                if (x1 > x2 - 1)
                    continue;
                drawRect(matrix, x1, y, x1 + 1, y + 1, 0xFF808080);
            }
        }
    }

    public static void blurArea(int x, int y, int width, int height, float intensity, float blurWidth, float blurHeight) {
        //TODO: implement
    }

    public static void drawGradientRect(MatrixStack matrix, float left, float top, float right, float bottom, boolean sideways, int startColor, int endColor) {
        if (Managers.TEXT.usingCustomFont()) {
            TextRenderer.FONTS.drawGradientRect(left, top, right - left, bottom - top, startColor, endColor);
        } else {
            float f = (startColor >> 24 & 255) / 255.0F;
            float f1 = (startColor >> 16 & 255) / 255.0F;
            float f2 = (startColor >> 8 & 255) / 255.0F;
            float f3 = (startColor & 255) / 255.0F;
            float f4 = (endColor >> 24 & 255) / 255.0F;
            float f5 = (endColor >> 16 & 255) / 255.0F;
            float f6 = (endColor >> 8 & 255) / 255.0F;
            float f7 = (endColor & 255) / 255.0F;
            BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            Matrix4f posMatrix = matrix.peek().getPositionMatrix();

            RenderSystem.setShader(GameRenderer::getPositionColorProgram);
            if (sideways) {
                bufferBuilder.vertex(posMatrix, left, top, 0.0F).color(f1, f2, f3, f);
                bufferBuilder.vertex(posMatrix, left, bottom, 0.0F).color(f1, f2, f3, f);
                bufferBuilder.vertex(posMatrix, right, bottom, 0.0F).color(f5, f6, f7, f4);
                bufferBuilder.vertex(posMatrix, right, top, 0.0F).color(f5, f6, f7, f4);
            } else {
                bufferBuilder.vertex(posMatrix, right, top, 0.0F).color(f1, f2, f3, f);
                bufferBuilder.vertex(posMatrix, left, top, 0.0F).color(f1, f2, f3, f);
                bufferBuilder.vertex(posMatrix, left, bottom, 0.0F).color(f5, f6, f7, f4);
                bufferBuilder.vertex(posMatrix, right, bottom, 0.0F).color(f5, f6, f7, f4);
            }
            BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        }
    }

    public static void drawPlayerFace(DrawContext context, PlayerEntity player, int x, int y, int width, int height) {
        if (player != null && mc.player != null) {
            if (mc.player.networkHandler == null) return;
            PlayerListEntry networkPlayerInfo = mc.player.networkHandler.getPlayerList().stream().filter(info -> info.getProfile().getName().equals(player.getName().getString())).findFirst().orElse(null);
            if (networkPlayerInfo == null) return;
            Identifier resourceLocation = networkPlayerInfo.getSkinTextures().texture();
            if (resourceLocation == null) return;

            RenderSystem.enableBlend();
            context.drawTexture(resourceLocation, x, y, 8, 8, width, height, 64, 64);
            RenderSystem.disableBlend();
        }
    }

    public static void scissor(float x, float y, float x1, float y1) {
        double sx = Math.min(x, x1);
        double sy = Math.min(y, y1);
        double w = Math.abs(x1 - x);
        double h = Math.abs(y1 - y);
        double height = mc.getWindow().getFramebufferHeight();
        double f = mc.getWindow().getScaleFactor();
        int px = (int) Math.round(sx * f);
        int py = (int) Math.round(height - f * (sy + h));
        int pw = (int) Math.round(f * w);
        int ph = (int) Math.round(f * h);
        if (Managers.TEXT.usingCustomFont()) {
            TextRenderer.FONTS.enableScissors(px, py, pw, ph);
        } else {
            GlStateManager._enableScissorTest();
            GlStateManager._scissorBox(px, py, pw, ph);
        }
    }

    public static void disableScissor() {
        if (Managers.TEXT.usingCustomFont()) {
            TextRenderer.FONTS.disableScissors();
        } else {
            GlStateManager._disableScissorTest();
        }
    }

    public static void drawPlayer(DrawContext context, PlayerEntity player, int playerScale, int x, int y, int width, int height) {
        InventoryScreen.drawEntity(context, x, y, x + width, y + height, Math.max(10, playerScale - 10), 0, (float) mc.mouse.getX(), (float) mc.mouse.getY(), player);
    }

    public static void drawItem(DrawContext context, ItemStack itemStack, int x, int y, boolean amount) {
        drawItem(context, itemStack, x, y, amount, false);
    }

    public static void drawItem(DrawContext context, ItemStack itemStack, int x, int y, boolean amount, boolean countUnstackable) {
        if (itemStack == null) return;
        int itemCount = itemStack.getCount();
        if (itemCount <= 0) {
            itemStack.setCount(1);
        }
        context.drawItem(itemStack, x, y);

        if (amount) {
            if (itemStack.isStackable() || countUnstackable || itemStack.getCount() > 1) {
                context.getMatrices().push();
                context.getMatrices().translate(0.0F, 0.0F, 200.0F);
                String count = TextUtil.numberFormatter(itemCount);
                Managers.TEXT.drawString(context, count,
                        x + 17 - Managers.TEXT.getStringWidth(count), y + 9,
                        HUD_EDITOR.get().matchColor.getValue()
                                ? HUD_EDITOR.get().color.getValue().getRGB()
                                : 0xffffffff,
                        true);
                context.getMatrices().pop();
            }
        }
    }

    public static boolean mouseWithinBounds(double mouseX, double mouseY, double x, double y, double width, double height) {
        return (mouseX >= x
                && mouseX <= (x + width))
                && (mouseY >= y
                && mouseY <= (y + height));
    }
}
