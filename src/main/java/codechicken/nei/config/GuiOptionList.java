package codechicken.nei.config;

import static codechicken.lib.gui.GuiDraw.drawMultilineTip;
import static codechicken.lib.gui.GuiDraw.drawStringC;
import static codechicken.lib.gui.GuiDraw.fontRenderer;
import static codechicken.lib.gui.GuiDraw.getMousePosition;
import static net.minecraft.util.StatCollector.translateToLocal;

import codechicken.core.gui.GuiCCButton;
import codechicken.core.gui.GuiScreenWidget;
import codechicken.core.gui.GuiScrollSlot;
import codechicken.lib.vec.Rectangle4i;
import codechicken.nei.LayoutManager;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

public class GuiOptionList extends GuiScreenWidget {
    public class OptionScrollSlot extends GuiScrollSlot {
        public final ArrayList<Option> options = new ArrayList<>();

        public OptionScrollSlot() {
            super(0, 0, 0, 0);
            setMargins(0, 4, 20, 4);
        }

        @Override
        public void onAdded(GuiScreen s) {
            super.onAdded(s);
            options.addAll(optionList.optionList);
            for (Option o : options) o.onAdded(this);
        }

        @Override
        public int getSlotHeight(int slot) {
            return options.get(slot).getHeight() + 4;
        }

        public int slotWidth() {
            return windowBounds().width - 24;
        }

        private Rectangle slotBounds(int slot) {
            return new Rectangle(24, 2, slotWidth(), getSlotHeight(slot) - 4);
        }

        @Override
        protected int getNumSlots() {
            return options.size();
        }

        @Override
        protected void drawSlot(int slot, int x, int y, int mx, int my, float frame) {
            Option o = options.get(slot);
            GL11.glTranslatef(x, y, 0);
            if (o.showWorldSelector() && world) drawWorldSelector(o, mx, my);
            GL11.glTranslatef(24, 2, 0);
            o.draw(mx - 24, my - 2, frame);
            GL11.glTranslatef(-x - 24, -y - 2, 0);
        }

        public Rectangle4i worldButtonSize() {
            return new Rectangle4i(0, 2, 20, 20);
        }

        private void drawWorldSelector(Option o, int mousex, int mousey) {
            Rectangle4i b = worldButtonSize();
            boolean set = o.hasWorldOverride();
            boolean mouseover = b.contains(mousex, mousey);
            GL11.glColor4f(1, 1, 1, 1);
            LayoutManager.drawButtonBackground(b.x, b.y, b.w, b.h, true, !set ? 0 : mouseover ? 2 : 1);
            drawStringC("W", b.x, b.y, b.w, b.h, -1);
        }

        @Override
        public void drawBackground(float frame) {
            Rectangle sbar = scrollbarBounds();
            drawRect(sbar.x, y, sbar.x + sbar.width, y + height, 0xFF000000);
        }

        @Override
        public void drawOverlay(float frame) {
            OptionScrollPane.drawOverlay(y, height, parentScreen.width, zLevel);
        }

        @Override
        public int scrollbarGuideAlignment() {
            return 0;
        }

        @Override
        public Dimension scrollbarDim() {
            Dimension dim = super.scrollbarDim();
            dim.width = 6;
            return dim;
        }

        @Override
        public void update() {
            super.update();
            for (Option o : options) o.update();
        }

        @Override
        protected void slotClicked(int slot, int button, int mx, int my, int count) {
            Option o = options.get(slot);
            if (world && o.showWorldSelector() && worldButtonSize().contains(mx, my)) {
                if (button == 1 && o.hasWorldOverride()) {
                    o.useGlobals();
                    Option.playClickSound();
                } else if (button == 0 && !o.hasWorldOverride()) {
                    o.copyGlobals();
                    Option.playClickSound();
                }
            } else if (slotBounds(slot).contains(mx, my)) {
                options.get(slot).mouseClicked(mx - 24, my - 2, button);
            }
        }

        @Override
        public void mouseClicked(int mx, int my, int button) {
            for (Option o : options) {
                o.onMouseClicked(mx, my, button);
            }

            super.mouseClicked(mx, my, button);
        }

        @Override
        public void keyTyped(char c, int keycode) {
            super.keyTyped(c, keycode);
            for (Option o : options) o.keyTyped(c, keycode);
        }

        public void resize() {
            int width = Math.min(parentScreen.width - 80, 320);
            setSize((parentScreen.width - width) / 2, 20, width, parentScreen.height - 50);
        }

        @Override
        public void mouseScrolled(int x, int y, int scroll) {
            scroll(-scroll);
        }

        public GuiOptionList getGui() {
            return ((GuiOptionList) parentScreen);
        }

        public List<String> handleTooltip(int mx, int my, List<String> tooltip) {
            int sy = my - y + scrolledPixels();
            int slot = getSlot(sy);
            if (slot >= 0) tooltip = handleTooltip(slot, mx - x, sy - getSlotY(slot), tooltip);
            return tooltip;
        }

        private List<String> handleTooltip(int slot, int mx, int my, List<String> tooltip) {
            Option o = options.get(slot);
            if (world && o.showWorldSelector() && worldButtonSize().contains(mx, my))
                tooltip.add(translateToLocal("nei.options.wbutton.tip." + (o.hasWorldOverride() ? "1" : "0")));
            if (slotBounds(slot).contains(mx, my)) return o.handleTooltip(mx - 24, my - 2, tooltip);
            return tooltip;
        }
    }

    protected final GuiScreen parent;
    protected final OptionList optionList;
    protected boolean world;

    protected OptionScrollSlot slot;
    protected GuiCCButton backButton;
    protected GuiCCButton worldButton;

    public GuiOptionList(GuiScreen parent, OptionList optionList, boolean world) {
        this.parent = parent;
        this.optionList = optionList;
        this.world = world;
    }

    @Override
    public void initGui() {
        xSize = width;
        ySize = height;
        super.initGui();
    }

    @Override
    public void resize() {
        slot.resize();
        backButton.width = Math.min(200, width - 40);
        backButton.x = (width - backButton.width) / 2;
        backButton.y = height - 25;
        worldButton.width = 60;
        worldButton.x = width - worldButton.width - 15;
    }

    @Override
    public void addWidgets() {
        add(slot = new OptionScrollSlot());
        add(backButton = new GuiCCButton(0, 0, 0, 20, translateToLocal("nei.options.back")).setActionCommand("back"));
        add(worldButton = new GuiCCButton(0, 2, 0, 16, worldButtonName()).setActionCommand("world"));
    }

    private String worldButtonName() {
        return translateToLocal("nei.options." + (world ? "world" : "global"));
    }

    @Override
    public void actionPerformed(String ident, Object... params) {
        if (ident.equals("back")) {
            if (parent instanceof GuiOptionList) ((GuiOptionList) parent).world = world;
            Minecraft.getMinecraft().displayGuiScreen(parent);
        } else if (ident.equals("world")) {
            world = !world;
            worldButton.text = worldButtonName();
        }
    }

    @Override
    public void drawBackground() {
        drawDefaultBackground();
    }

    @Override
    public void drawForeground() {
        drawCenteredString(fontRenderer, translateToLocal(optionList.fullName()), width / 2, 6, -1);
        drawTooltip();
    }

    private void drawTooltip() {
        List<String> tooltip = new LinkedList<>();
        Point mouse = getMousePosition();
        if (worldButton.pointInside(mouse.x, mouse.y))
            tooltip.addAll(Arrays.asList(translateToLocal("nei.options.global.tip." + (world ? "1" : "0"))
                    .split(":")));

        tooltip = slot.handleTooltip(mouse.x, mouse.y, tooltip);
        drawMultilineTip(mouse.x + 12, mouse.y - 12, tooltip);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void keyTyped(char c, int keycode) {
        if (keycode == Keyboard.KEY_ESCAPE) {
            GuiScreen p = parent;
            while (p instanceof GuiOptionList) p = ((GuiOptionList) p).parent;

            Minecraft.getMinecraft().displayGuiScreen(p);
        } else {
            super.keyTyped(c, keycode);
        }
    }

    public boolean worldConfig() {
        return world;
    }
}
