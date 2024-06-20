package net.thenextlvl.gopaint.menu;

import core.paper.gui.PagedGUI;
import core.paper.item.ActionItem;
import core.paper.item.ItemBuilder;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.thenextlvl.gopaint.api.brush.Brush;
import net.thenextlvl.gopaint.api.brush.setting.PlayerBrushSettings;
import net.thenextlvl.gopaint.api.model.GoPaintProvider;
import net.thenextlvl.gopaint.util.Items;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.stream.IntStream;

public class BrushesMenu extends PagedGUI<GoPaintProvider, Brush> {
    private final @Getter Options options = new Options(
            IntStream.range(0, getSize() - 9).toArray(),
            getSize() - 6,
            getSize() - 4
    );
    private final PlayerBrushSettings settings;

    public BrushesMenu(GoPaintProvider plugin, PlayerBrushSettings settings, Player owner) {
        super(plugin, owner, plugin.bundle().component(owner, "menu.brushes.title"), 3);
        this.settings = settings;
        loadPage(0);
    }

    @Override
    public void formatDefault() {
        var placeholder = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).hideTooltip(true);
        IntStream.range(0, getSize()).forEach(value -> setSlotIfAbsent(value, placeholder));
    }

    @Override
    public ActionItem constructItem(Brush brush) {
        return Items.createHead(brush.getHeadValue(), 1, "§6" + brush.getName(),
                "\n§7Click to select\n\n§8" + brush.getDescription()
        ).withAction(() -> {
            settings.setBrush(brush);
            settings.getMainMenu().open();
        });
    }

    @Override
    public Component getPageFormat(int page) {
        var key = page > getCurrentPage() ? "gui.page.next" : "gui.page.previous";
        return plugin.bundle().component(owner, key);
    }

    @Override
    public Collection<Brush> getElements() {
        return plugin.brushRegistry().getBrushes().toList();
    }
}
