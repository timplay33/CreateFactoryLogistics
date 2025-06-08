package ru.zznty.create_factory_logistics.logistics.panel;

import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.utility.CreateLang;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import ru.zznty.create_factory_logistics.CreateFactoryLogistics;
import ru.zznty.create_factory_logistics.FactoryBlocks;

import java.util.Collections;
import java.util.List;

public class FactoryPanelSetFluidScreen extends AbstractSimiContainerScreen<FactoryPanelSetFluidMenu> {
    private IconButton confirmButton;
    private List<Rect2i> extraAreas = Collections.emptyList();

    public FactoryPanelSetFluidScreen(FactoryPanelSetFluidMenu container, Inventory inv, Component title) {
        super(container, inv, title);
    }

    @Override
    protected void init() {
        int bgHeight = AllGuiTextures.FACTORY_GAUGE_SET_ITEM.getHeight();
        int bgWidth = AllGuiTextures.FACTORY_GAUGE_SET_ITEM.getWidth();
        setWindowSize(bgWidth, bgHeight + AllGuiTextures.PLAYER_INVENTORY.getHeight());
        super.init();
        clearWidgets();
        int x = getGuiLeft();
        int y = getGuiTop();

        confirmButton = new IconButton(x + bgWidth - 40, y + bgHeight - 25, AllIcons.I_CONFIRM);
        confirmButton.withCallback(() -> minecraft.player.closeContainer());
        addRenderableWidget(confirmButton);

        extraAreas = List.of(new Rect2i(x + bgWidth, y + bgHeight - 30, 40, 20));
    }

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        int x = getGuiLeft();
        int y = getGuiTop();
        AllGuiTextures.FACTORY_GAUGE_SET_ITEM.render(pGuiGraphics, x - 5, y);
        renderPlayerInventory(pGuiGraphics, x + 5, y + 94);

        ItemStack stack = FactoryBlocks.FACTORY_FLUID_GAUGE.asStack();
        Component title = CreateLang.builder(CreateFactoryLogistics.MODID)
                .translate("gui.factory_fluid_panel.place_fluid_to_monitor")
                .component();
        pGuiGraphics.drawString(font, title, x + imageWidth / 2 - font.width(title) / 2 - 5, y + 4, 0x3D3C48, false);

        GuiGameElement.of(stack)
                .scale(3)
                .render(pGuiGraphics, x + 180, y + 48);
    }

    @Override
    public List<Rect2i> getExtraAreas() {
        return extraAreas;
    }
}
