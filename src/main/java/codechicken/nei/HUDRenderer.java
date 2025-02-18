package codechicken.nei;

import static codechicken.lib.gui.GuiDraw.displaySize;
import static codechicken.lib.gui.GuiDraw.drawString;
import static codechicken.lib.gui.GuiDraw.drawTooltipBox;
import static codechicken.lib.gui.GuiDraw.getStringWidth;

import codechicken.lib.config.ConfigTag;
import codechicken.nei.KeyManager.IKeyStateTracker;
import codechicken.nei.api.ItemInfo;
import codechicken.nei.guihook.GuiContainerManager;
import codechicken.nei.recipe.StackInfo;
import java.awt.Dimension;
import java.awt.Point;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class HUDRenderer implements IKeyStateTracker {
    @Override
    public void tickKeyStates() {
        if (KeyManager.keyStates.get("world.highlight_tips").down) {
            ConfigTag tag = NEIClientConfig.getSetting("world.highlight_tips");
            tag.setBooleanValue(!tag.getBooleanValue());
        }
    }

    public static void renderOverlay() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.currentScreen == null
                && mc.theWorld != null
                && !mc.gameSettings.keyBindPlayerList.getIsKeyPressed()
                && NEIClientConfig.getBooleanSetting("world.highlight_tips")
                && mc.objectMouseOver != null
                && mc.objectMouseOver.typeOfHit == MovingObjectType.BLOCK) {
            World world = mc.theWorld;
            ItemStack[] items = ItemInfo.getIdentifierItems(world, mc.thePlayer, mc.objectMouseOver)
                    .toArray(new ItemStack[0]);

            if (items.length == 0) return;

            ItemStack stack = StackInfo.getItemStackWithMinimumDamage(items);

            renderOverlay(stack, ItemInfo.getText(stack, world, mc.thePlayer, mc.objectMouseOver), getPositioning());
        }
    }

    public static void renderOverlay(ItemStack stack, List<String> textData, Point pos) {
        if ((stack == null || stack.getItem() == null) && textData.isEmpty()) return;

        int w = 0;
        for (String s : textData) w = Math.max(w, getStringWidth(s) + 29);
        int h = Math.max(24, 10 + 10 * textData.size());

        Dimension size = displaySize();
        int x = (size.width - w - 1) * pos.x / 10000;
        int y = (size.height - h - 1) * pos.y / 10000;

        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        drawTooltipBox(x, y, w, h);

        int ty = (h - 8 * textData.size()) / 2;
        for (int i = 0; i < textData.size(); i++)
            drawString(textData.get(i), x + 24, y + ty + 10 * i, 0xFFA0A0A0, true);

        RenderHelper.enableGUIStandardItemLighting();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);

        if (stack != null && stack.getItem() != null) GuiContainerManager.drawItem(x + 5, y + h / 2 - 8, stack);
    }

    private static Point getPositioning() {
        return new Point(
                NEIClientConfig.getSetting("world.highlight_tips.x").getIntValue(),
                NEIClientConfig.getSetting("world.highlight_tips.y").getIntValue());
    }

    public static void load() {
        KeyManager.trackers.add(new HUDRenderer());
    }
}
